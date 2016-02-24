/*
 * Copyright (c) 2010, Sun Microsystems, Inc.
 * Copyright (c) 2010, The Storage Networking Industry Association.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of The Storage Networking Industry Association (SNIA) nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 *  THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.snia.cdmiserver.dao.filesystem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.snia.cdmiserver.dao.ContainerDao;
import org.snia.cdmiserver.exception.BadRequestException;
import org.snia.cdmiserver.exception.NotFoundException;
import org.snia.cdmiserver.model.Container;
import org.snia.cdmiserver.util.ObjectID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Concrete implementation of {@link ContainerDao} using the local filesystem as the backing store.
 * </p>
 */
public class ContainerDaoImpl implements ContainerDao {

    private static final Logger LOG = LoggerFactory.getLogger(ContainerDaoImpl.class);

    //
    // Properties and Dependency Injection Methods
    //
    private String baseDirectoryName = null;

    /**
     * <p>
     * Set the base directory name for our local storage.
     * </p>
     *
     * @param baseDirectory
     *            The new base directory name
     */
    public void setBaseDirectoryName(String baseDirectoryName) {
        this.baseDirectoryName = baseDirectoryName;
    }

    private boolean recreate = false;

    /**
     * <p>
     * Set the "recreate on first use" flag that (if set) will cause any previous contents of the
     * base directory to be erased on first access. Default value for this flag is
     * <code>false</code>.
     * </p>
     *
     * @param recreate
     *            The new recreate flag value
     */
    public void setRecreate(boolean recreate) {
        this.recreate = recreate;
    }

    //
    // ContainerDao Methods invoked from PathResource
    //
    @Override
    public Container createByPath(String path, Container containerRequest) {

        //
        // The User metadata and exports have already been de-serialized into the
        // passed Container in PathResource.putContainer()
        //

        File directory = absoluteFile(path);

        File containerFieldsFile = getContainerFieldsFile(path);

        if (containerRequest.getMove() == null) { // This is a normal Create or Update

            //
            // Setup ISO-8601 Date
            //
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

            //
            // Underlying Directory existence determines whether this is a Create or
            // Update.
            //

            if (!directory.exists()) { // Creating Container

                if (!directory.mkdir()) {
                    throw new IllegalArgumentException("Cannot create container '" + path + "'");
                }

                String objectID = ObjectID.getObjectID(9);// System.nanoTime()+"";
                containerRequest.setObjectID(objectID);

                //
                // TODO: Use Parent capabiltiesURI if not specified in create body
                //

                containerRequest.setCapabilitiesURI("/cdmi_capabilities/container/default");

                //
                // TODO: Use Parent Domain if not specified in create body
                //
                if (containerRequest.getDomainURI() == null)
                    containerRequest.setDomainURI("/cdmi_domains/default_domain");

                Map<String, Object> exports = containerRequest.getExports();
                if (exports.containsKey("OCCI/NFS")) {
                    // Export this directory (OpenSolaris only so far)
                    // Runtime runtime = Runtime.getRuntime();
                    // String exported =
                    // "pfexec share -f nfs -o rw=10.1.254.117:10.1.254.122:10.1.254.123:10.1.254.124:10.1.254.125:10.1.254.126:10.1.254.127 "
                    // + containerFieldsFile.getAbsolutePath();
                    // runtime.exec(exported);
                }

                containerRequest.getMetadata().put("cdmi_ctime", sdf.format(now));
                containerRequest.getMetadata().put("cdmi_mtime", "never");
                containerRequest.getMetadata().put("cdmi_atime", "never");
                containerRequest.getMetadata().put("cdmi_acount", "0");
                containerRequest.getMetadata().put("cdmi_mcount", "0");

            } else { // Updating Container

                //
                // Read the persistent metatdata from the "." file
                //
                Container currentContainer = getPersistedContainerFields(containerFieldsFile);

                containerRequest.setObjectID(currentContainer.getObjectID());

                //
                // TODO: Need to handle update of Domain
                //

                Map<String, Object> exports = containerRequest.getExports();
                if (exports.containsKey("OCCI/NFS")) {
                    if (currentContainer.getExports().containsKey("OCCI/NFS")) {
                        // Do nothing - already exported
                    } else {
                    }
                }

                containerRequest.getMetadata().put(
                        "cdmi_ctime",
                        currentContainer.getMetadata().get("cdmi_ctime"));
                containerRequest.getMetadata().put(
                        "cdmi_atime",
                        currentContainer.getMetadata().get("cdmi_atime"));
                containerRequest.getMetadata().put("cdmi_mtime", sdf.format(now));
            }

            //
            // Write created or updated persisted fields out to the "." file
            //

            try {
                FileWriter fstream = new FileWriter(containerFieldsFile.getAbsolutePath());
                try (BufferedWriter out = new BufferedWriter(fstream)) {
                    out.write(containerRequest.toJson(true)); // Save it
                }
            } catch (Exception ex) {
                LOG.error("Exception while writing", ex);
                throw new IllegalArgumentException("Cannot write container fields file @"
                                                   + path
                                                   + " error : "
                                                   + ex);
            }

            //
            // Transient fields
            //
            containerRequest.setCompletionStatus("Complete");

            //
            // Complete response with fields dynamically generated from directory info.
            //

            return completeContainer(containerRequest, directory, path);

        } else { // Moving a Container

            if (directory.exists()) {
                throw new IllegalArgumentException("Cannot move container '"
                                                   + containerRequest.getMove()
                                                   + "' to '"
                                                   + path
                                                   + "'; Destination already exists");
            }

            File sourceContainerFile = absoluteFile(containerRequest.getMove());

            if (!sourceContainerFile.exists()) {
                throw new NotFoundException("Path '"
                                            + directory.getAbsolutePath()
                                            + "' does not identify an existing container");
            }
            if (!sourceContainerFile.isDirectory()) {
                throw new IllegalArgumentException("Path '"
                                                   + directory.getAbsolutePath()
                                                   + "' does not identify a container");
            }

            //
            // Move Container directory
            //

            sourceContainerFile.renameTo(directory);

            //
            // Move Container's Metadata .file
            //

            File sourceContainerFieldsFile = getContainerFieldsFile(containerRequest.getMove());

            sourceContainerFieldsFile.renameTo(containerFieldsFile);

            //
            // Get the containers field's to return in response
            //

            Container movedContainer = getPersistedContainerFields(containerFieldsFile);

            //
            // If the request has a metadata field, replace any metadata filed in the source
            // Container
            //

            if (!containerRequest.getMetadata().isEmpty()) {
                String cdmi_ctime = movedContainer.getMetadata().get("cdmi_ctime");
                String cdmi_mtime = movedContainer.getMetadata().get("cdmi_mtime");
                String cdmi_atime = movedContainer.getMetadata().get("cdmi_atime");
                String cdmi_acount = movedContainer.getMetadata().get("cdmi_acount");
                String cdmi_mcount = movedContainer.getMetadata().get("cdmi_mcount");

                movedContainer.setMetaData(containerRequest.getMetadata());

                movedContainer.getMetadata().put("cdmi_ctime", cdmi_ctime);
                movedContainer.getMetadata().put("cdmi_mtime", cdmi_mtime);
                movedContainer.getMetadata().put("cdmi_atime", cdmi_atime);
                movedContainer.getMetadata().put("cdmi_acount", cdmi_acount);
                movedContainer.getMetadata().put("cdmi_mcount", cdmi_mcount);

                //
                // Write created or updated persisted fields out to the "." file
                //

                try {
                    FileWriter fstream = new FileWriter(containerFieldsFile.getAbsolutePath());
                    try (BufferedWriter out = new BufferedWriter(fstream)) {
                        out.write(containerRequest.toJson(true)); // Save it
                    }
                } catch (Exception ex) {
                    LOG.error("Exception while writing", ex);
                    throw new IllegalArgumentException("Cannot write container fields file @"
                                                       + path
                                                       + " error : "
                                                       + ex);
                }

            }

            //
            // Transient fields
            //

            movedContainer.setCompletionStatus("Complete");

            //
            // Complete response with fields dynamically generated from directory info.
            //

            return completeContainer(movedContainer, directory, path);
        }

    }

    //
    // For now this method supports both Container and Object delete.
    //
    // Improper requests directed at the root container are not routed here by
    // PathResource.
    //
    @Override
    public void deleteByPath(String path) {
        File directoryOrFile = absoluteFile(path);

        //

        if (directoryOrFile.isDirectory()) {
            recursivelyDelete(directoryOrFile);
        } else {
            directoryOrFile.delete();
        }

        //
        // remove the "." file that contains the Container or Object's JSON-encoded
        // metadata
        //
        getContainerFieldsFile(path).delete();
    }

    //
    // Not Implemented
    //
    @Override
    public Container findByObjectId(String objectId) {
        throw new UnsupportedOperationException("ContainerDaoImpl.findByObjectId()");
    }

    //
    //
    //
    @Override
    public Container findByPath(String path) {

        LOG.trace("In ContainerDAO.findByPath : {}", path);

        File directory = absoluteFile(path);

        if (!directory.exists()) {
            throw new NotFoundException("Path '"
                                        + directory.getAbsolutePath()
                                        + "' does not identify an existing container");
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Path '"
                                               + directory.getAbsolutePath()
                                               + "' does not identify a container");
        }

        Container requestedContainer = new Container();

        if (path != null) {

            //
            // Read the persisted container fields from the "." file
            //
            requestedContainer = getPersistedContainerFields(getContainerFieldsFile(path));

        } else {

            //
            // if this is the root container there is no "." metadata file up one level.
            // Dynamically generate the default values
            //

            requestedContainer.setCapabilitiesURI("/cdmi_capabilities/container/default");
            requestedContainer.setDomainURI("/cdmi_domains/default_domain");
        }

        return completeContainer(requestedContainer, directory, path);
    }

    //
    // Private Helper Methods
    //
    /**
     * <p>
     * Return a {@link File} instance for the container fields file object.
     * </p>
     *
     * @param path
     *            Path of the requested container.
     */
    private File getContainerFieldsFile(String path) {
        // path should be /<parent container name>/<container name>
        String[] tokens = path.split("[/]+");
        if (tokens.length < 1) {
            throw new BadRequestException("No object name in path <" + path + ">");
        }
        String containerName = tokens[tokens.length - 1];
        String containerFieldsFileName = "." + containerName;
        // piece together parent container name
        // FIXME : This is the kludge way !
        String parentContainerName = "";
        for (int i = 0; i <= tokens.length - 2; i++) {
            parentContainerName += tokens[i] + "/";
        }
        LOG.trace("Path = {}", path);
        LOG.trace("Parent Container Name = {} Container Name == {}",
                           parentContainerName, containerName);


        File baseDirectory1, parentContainerDirectory, containerFieldsFile;
        try {
            LOG.trace("baseDirectory = {}", baseDirectoryName);
            baseDirectory1 = new File(baseDirectoryName + "/");
            LOG.trace("Base Directory Absolute Path = {}", baseDirectory1.getAbsolutePath());
            parentContainerDirectory = new File(baseDirectory1, parentContainerName);
            //
            LOG.trace("Parent Container Absolute Path = {}",
                               parentContainerDirectory.getAbsolutePath());
            //
            containerFieldsFile = new File(parentContainerDirectory, containerFieldsFileName);
            LOG.trace("Container Metadata File Path = {}",
                               containerFieldsFile.getAbsolutePath());
        } catch (Exception ex) {
            LOG.error("Exception while building File objects: ", ex);
            throw new IllegalArgumentException("Cannot build Object @" + path + " error : " + ex);
        }
        return containerFieldsFile;
    }

    /**
     * <p>
     * Return a {@link Container} instance for the container fields.
     * </p>
     *
     * @param containerFieldsFile
     *            File object for the container fields file.
     */
    private Container getPersistedContainerFields(File containerFieldsFile) {
        Container containerFields = new Container();
        try {
            try (FileInputStream in = new FileInputStream(containerFieldsFile.getAbsolutePath())) {
                int inpSize = in.available();
                LOG.trace("Container fields file size: {}", inpSize);

                byte[] inBytes = new byte[inpSize];
                in.read(inBytes);

                containerFields.fromJson(inBytes, true);
                String mds = new String(inBytes);
                LOG.trace("Container fields read were: {}", mds);
            }
        } catch (Exception ex) {
            LOG.error("Exception while reading: ", ex);
            throw new IllegalArgumentException("Cannot read container fields file error : " + ex);
        }
        return containerFields;
    }

    /**
     * <p>
     * Return a {@link File} instance for the file or directory at the specified path from our base
     * directory.
     * </p>
     *
     * @param path
     *            Path of the requested file or directory.
     */
    public File absoluteFile(String path) {
        if (path == null) {
            return baseDirectory();
        } else {
            return new File(baseDirectory(), path);
        }
    }

    private File baseDirectory = null;

    /**
     * <p>
     * Return a {@link File} instance for the base directory, erasing any previous content on first
     * use if the <code>recreate</code> flag has been set.
     * </p>
     *
     * @exception IllegalArgumentException
     *                if we cannot create the base directory
     */
    private File baseDirectory() {
        if (baseDirectory == null) {
            baseDirectory = new File(baseDirectoryName);
            if (recreate) {
                recursivelyDelete(baseDirectory);
                if (!baseDirectory.mkdirs()) {
                    throw new IllegalArgumentException("Cannot create base directory '"
                                                       + baseDirectoryName
                                                       + "'");
                }
            }
        }
        return baseDirectory;
    }

    /**
     * <p>
     * Return the {@link Container} identified by the specified <code>path</code>.
     * </p>
     *
     * @param container
     *            The requested container with persisted fields
     * @param directory
     *            Directory of the requested container
     * @param path
     *            Path of the requested container
     *
     * @exception NotFoundException
     *                if the specified path does not identify a valid resource
     * @exception IllegalArgumentException
     *                if the specified path identifies a data object instead of a container
     */
    private Container completeContainer(Container container, File directory, String path) {
        LOG.trace("In ContainerDaoImpl.Container, path is: {}", path);

        LOG.trace("In ContainerDaoImpl.Container, absolute path is: {}",
                           directory.getAbsolutePath());


        container.setObjectType("application/cdmi-container");



        //
        // Derive ParentURI
        //

        String parentURI = "/";

        if (path != null) {
            String[] tokens = path.split("[/]+");
            String containerName = tokens[tokens.length - 1];
            // FIXME : This is the kludge way !
            for (int i = 0; i <= tokens.length - 2; i++) {
                parentURI += tokens[i] + "/";
            }
            LOG.trace("In ContainerDaoImpl.Container, ParentURI = {}"
                               + " Container Name = {}", parentURI, containerName);
            // Check for illegal top level container names
            if (parentURI.matches("/") && containerName.startsWith("cdmi")) {
                throw new BadRequestException("Root container names must not start with cdmi");
            }
        }

        container.setParentURI(parentURI);

        //
        // Add children containers and/or objects representing subdirectories or
        // files
        //

        List<String> children = container.getChildren();

        for (File file : directory.listFiles()) {
            String name = file.getName();
            if (file.isDirectory()) {
                children.add(name + "/");
            } else {
                if (!file.getName().startsWith(".")) {
                    children.add(name);
                }
            }
        }

        if (children.size() > 0) {
            // has children - set the range
            int lastindex = children.size() - 1;
            String childrange = "0-" + lastindex;
            container.setChildrenrange(childrange);
        }

        return container;
    }

    /**
     * <p>
     * Delete the specified directory, after first recursively deleting any contents within it.
     * </p>
     *
     * @param directory
     *            {@link File} identifying the directory to be deleted
     */
    private void recursivelyDelete(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                recursivelyDelete(file);
            } else {
                file.delete();
            }
        }
        directory.delete();
    }

    //

    @Override
    public boolean isContainer(String path) {
        File directoryOrFile = absoluteFile(path);
        return directoryOrFile.isDirectory();
    }
}
