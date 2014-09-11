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

import org.snia.cdmiserver.dao.ContainerDao;
import org.snia.cdmiserver.dao.DataObjectDao;
import org.snia.cdmiserver.exception.BadRequestException;
import org.snia.cdmiserver.exception.ConflictException;
import org.snia.cdmiserver.model.DataObject;
import org.snia.cdmiserver.util.ObjectID;

/**
 * <p>
 * Concrete implementation of {@link DataObjectDao} using the local filesystem as the backing store.
 * </p>
 */
public class DataObjectDaoImpl implements DataObjectDao {

    // -------------------------------------------------------------- Properties
    private String baseDirectoryName = null;

    //

    public void setBaseDirectoryName(String baseDirectoryName) {
        this.baseDirectoryName = baseDirectoryName;
        System.out.println("******* Base Directory = " + baseDirectoryName);
    }

    /**
     * <p>
     * Injected {@link ContainerDao} instance.
     * </p>
     */
    private ContainerDao containerDao;

    // private Map<String,DataObject> dataDB =
    // new ConcurrentHashMap<String, DataObject>();
    public void setContainerDao(ContainerDao containerDao) {
        this.containerDao = containerDao;
    }

    // ---------------------------------------------------- ContainerDao Methods
    // utility function
    // given a path, find out metadata file name and container directory
    String getmetadataFileName(String path) {
        // Make sure we have a file name for the object
        // check for file name
        // path should be <container name>/<file name>
        // Split path into path and filename
        String[] tokens = path.split("/");
        if (tokens.length < 1) {
            throw new BadRequestException("No object name in path <" + path + ">");
        }
        String fileName = tokens[tokens.length - 1];
        return "." + fileName;
    }

    //
    String getcontainerName(String path) {
        // Make sure we have a file name for the object
        // check for file name
        // path should be <container name>/<file name>
        // Split path into path and filename
        String[] tokens = path.split("/");
        if (tokens.length < 1) {
            throw new BadRequestException("No object name in path <" + path + ">");
        }
        String fileName = tokens[tokens.length - 1];
        String containerName = "";
        for (int i = 0; i <= tokens.length - 2; i++) {
            containerName += tokens[i] + "/";
        }
        return containerName;
    }

    @Override
    public DataObject createByPath(String path, DataObject dObj) throws Exception {
        //
        String metadataFileName = getmetadataFileName(path);
        String containerName = getcontainerName(path);
        //
        File objFile, baseDirectory, containerDirectory, metadataFile;
        try {
            System.out.println("baseDirectory = " + baseDirectoryName);
            baseDirectory = new File(baseDirectoryName + "/");
            System.out.println("Base Directory Absolute Path = " + baseDirectory.getAbsolutePath());
            containerDirectory = new File(baseDirectory, containerName);
            // File directory = absoluteFile(path);
            System.out.println("Container Absolute Path = " + containerDirectory.getAbsolutePath());
            //
            metadataFile = new File(containerDirectory, metadataFileName);
            System.out.println("Metadada File Path = " + metadataFile.getAbsolutePath());
            objFile = new File(baseDirectory, path);
            // File directory = absoluteFile(path);
            System.out.println("Object Absolute Path = " + objFile.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception while writing: " + ex);
            throw new IllegalArgumentException("Cannot write Object @" + path + " error : " + ex);
        }
        // check for container
        if (!containerDirectory.exists()) {
            throw new ConflictException("Container <"
                                        + containerDirectory.getAbsolutePath()
                                        + "> doesn't exist");
        }
        if (objFile.exists()) {
            throw new ConflictException("Object File <" + objFile.getAbsolutePath() + "> exists");
        }
        try {
            // dObj.setObjectURI(path); // TBD Correct
            // Make object ID
            String objectID = dObj.getObjectID();
            if (objectID == null) {
                objectID = ObjectID.getObjectID(8);//System.nanoTime()+"";
                dObj.setObjectID(objectID);
            }
            // dObj.setObjectURI(directory.getAbsolutePath()+"/"+objectID);
            dObj.setCapabilitiesURI("/cdmi_capabilities/dataobject");
            // Add metadata
            String val = "" + dObj.getValue();
            dObj.setMetadata("cdmi_size", val.length() + "");
            // ISO-8601 Date
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            dObj.setMetadata("cdmi_ctime", sdf.format(now));
            dObj.setMetadata("cdmi_atime", "never");
            // dObj.setMetadata("cdmi_acount", "0");
            // dObj.setMetadata("cdmi_mcount", "0");
            // Create file
            dObj.setMetadata("fileName", objFile.getAbsolutePath());
            dObj.setMetadata("metadataFileName", metadataFile.getAbsolutePath());
            String mimeType = dObj.getMimetype();
            if (mimeType == null) {
                mimeType = "text/plain";
                dObj.setMimetype(mimeType);
            }
            dObj.setMetadata("mimetype", mimeType);
            //
            FileWriter fstream = new FileWriter(objFile.getAbsolutePath());
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(dObj.getValue()); // Save Only the value
            // Close the output stream
            out.close();
            // write metadata file
            System.out.println("metadataFile : " + metadataFileName);
            fstream = new FileWriter(metadataFile.getAbsolutePath());
            out = new BufferedWriter(fstream);
            out.write(dObj.metadataToJson()); // Save it
            // Close the output stream
            out.close();
            //
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception while writing: " + ex);
            throw new IllegalArgumentException("Cannot write Object @" + path + " error : " + ex);
        }
        return dObj;
    }
    

    @Override
    public DataObject createNonCDMIByPath(String path, String contentType, DataObject dObj) throws Exception {
        //
        String metadataFileName = getmetadataFileName(path);
        String containerName = getcontainerName(path);
        //
        File objFile, baseDirectory, containerDirectory, metadataFile;
        try {
            System.out.println("createNonCDMIByPath baseDirectory = " + baseDirectoryName);
            baseDirectory = new File(baseDirectoryName + "/");
            System.out.println("createNonCDMIByPath Base Directory Absolute Path = " + baseDirectory.getAbsolutePath());
            containerDirectory = new File(baseDirectory, containerName);
            // File directory = absoluteFile(path);
            System.out.println("createNonCDMIByPath Container Absolute Path = " + containerDirectory.getAbsolutePath());
            //
            metadataFile = new File(containerDirectory, metadataFileName);
            System.out.println("createNonCDMIByPath Metadada File Path = " + metadataFile.getAbsolutePath());
            objFile = new File(baseDirectory, path);
            // File directory = absoluteFile(path);
            System.out.println("createNonCDMIByPath Object Absolute Path = " + objFile.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception while writing: " + ex);
            throw new IllegalArgumentException("Cannot write Object @" + path + " error : " + ex);
        }
        // check for container
        if (!containerDirectory.exists()) {
            throw new ConflictException("Container <"
                                        + containerDirectory.getAbsolutePath()
                                        + "> doesn't exist");
        }
        if (objFile.exists()) {
            throw new ConflictException("Object File <" + objFile.getAbsolutePath() + "> exists");
        }
        try {
            // dObj.setObjectURI(path); // TBD Correct
            // Make object ID
            String objectID = dObj.getObjectID();
            if (objectID == null) {
                objectID = ObjectID.getObjectID(8);//System.nanoTime()+"";
                dObj.setObjectID(objectID);
            }
            // dObj.setObjectURI(directory.getAbsolutePath()+"/"+objectID);
            dObj.setCapabilitiesURI("/cdmi_capabilities/dataobject");
            // Add metadata
            String val = "" + dObj.getValue();
            dObj.setMetadata("cdmi_size", val.length() + "");
            // ISO-8601 Date
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            dObj.setMetadata("cdmi_ctime", sdf.format(now));
            dObj.setMetadata("cdmi_atime", "never");
            // dObj.setMetadata("cdmi_acount", "0");
            // dObj.setMetadata("cdmi_mcount", "0");
            // Create file
            dObj.setMetadata("fileName", objFile.getAbsolutePath());
            dObj.setMetadata("metadataFileName", metadataFile.getAbsolutePath());

            dObj.setMimetype(contentType);
            System.out.println("createNonCDMIByPath Content Type : " + contentType);

            dObj.setMetadata("mimetype", contentType);
            //
            FileWriter fstream = new FileWriter(objFile.getAbsolutePath());
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(dObj.getValue()); // Save Only the value
            // Close the output stream
            out.close();
            // write metadata file
            System.out.println("metadataFile : " + metadataFileName);
            fstream = new FileWriter(metadataFile.getAbsolutePath());
            out = new BufferedWriter(fstream);
            out.write(dObj.metadataToJson()); // Save it
            // Close the output stream
            out.close();
            //
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception while writing: " + ex);
            throw new IllegalArgumentException("Cannot write Object @" + path + " error : " + ex);
        }
        return dObj;
    }
    


    @Override
    public DataObject createById(String objectId, DataObject dObj) {
        throw new UnsupportedOperationException("DataObjectDaoImpl.createById()");
    }

    @Override
    public void deleteByPath(String path) {
        throw new UnsupportedOperationException("DataObjectDaoImpl.deleteByPath()");
    }

    @Override
    public DataObject findByPath(String path) {
        System.out.println("In findByPath : " + path);
        //
        String metadataFileName = getmetadataFileName(path);
        String containerName = getcontainerName(path);
        //
        // Check for metadata file
        File objFile, metadataFile, baseDirectory;
        try {
            System.out.println("baseDirectory = " + baseDirectoryName);
            baseDirectory = new File(baseDirectoryName + "/" + containerName);
            metadataFile = new File(baseDirectory, metadataFileName);
            System.out.println("Metadata Absolute Path = " + metadataFile.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception in findByPath : " + ex);
            throw new IllegalArgumentException("Cannot get Object @" + path + " error : " + ex);
        }
        if (!metadataFile.exists()) {
            return null;
        }
        // Check for object file
        try {
            System.out.println("baseDirectory = " + baseDirectoryName);
            baseDirectory = new File(baseDirectoryName + "/");
            objFile = new File(baseDirectory, path);
            System.out.println("Object Absolute Path = " + objFile.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception in findByPath : " + ex);
            throw new IllegalArgumentException("Cannot get Object @" + path + " error : " + ex);
        }
        if (!objFile.exists()) {
            throw new ConflictException("Object File <"
                                        + objFile.getAbsolutePath()
                                        + "> doesn't exist");
        }
        //
        // Both Files are there. So open, read, create object and send out
        //
        DataObject dObj = new DataObject();
        try {
            // Read metadata
            FileInputStream in = new FileInputStream(metadataFile.getAbsolutePath());
            int inpSize = in.available();
            byte[] inBytes = new byte[inpSize];
            in.read(inBytes);
            dObj.fromJson(inBytes, true);
            // Close the output stream
            in.close();
            // Read object from file
            in = new FileInputStream(objFile.getAbsolutePath());
            inpSize = in.available();
            inBytes = new byte[inpSize];
            in.read(inBytes);
            dObj.setValue(new String(inBytes));
            // Close the output stream
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception while reading: " + ex);
            throw new IllegalArgumentException("Cannot read Object @" + path + " error : " + ex);
        }

        if (dObj != null) {
            // change access time
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            dObj.setMetadata("cdmi_atime", sdf.format(now));
            // need to increment acount dObj.setMetadata("cdmi_acount", "0");
        }
        return dObj;
        // throw new UnsupportedOperationException("DataObjectDaoImpl.findByPath()");
    }

    @Override
    public DataObject findByObjectId(String objectId) {
        throw new UnsupportedOperationException("DataObjectDaoImpl.findByObjectId()");
    }
    // --------------------------------------------------------- Private Methods
}
