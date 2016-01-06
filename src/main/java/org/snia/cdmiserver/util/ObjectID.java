/*
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

package org.snia.cdmiserver.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ksankar May 29,2010
 */
public class ObjectID {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectID.class);

    public static String getObjectID(int eNum) {
        byte objBytes[] = new byte[24];
        String objID = "";
        // Create an Object ID as per CDMI Spec v1.0 Section 5.11
        //
        // 0EEE0LCCDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD
        // 0123456789012345678901234567890123456789
        // 1 2 3
        // E = Enterprise Number, L=Length in bytes,D=Opaque ID
        // Max Length = 40 bytes. But we use 24 Bytes
        //
        // encode in hex
        //
        // FIXME : Check for eNum != 0
        // FIXME : Is it in network byte order ?
        //
        objBytes[0] = (byte) (eNum >> 24);
        objBytes[1] = (byte) (eNum >> 16);
        objBytes[2] = (byte) (eNum >> 8);
        objBytes[3] = (byte) eNum;
        objBytes[4] = 0;
        objBytes[5] = 24;// length
        objBytes[6] = 0; // CRC
        objBytes[7] = 0; // CRC
        //
        java.util.UUID uuid = java.util.UUID.randomUUID();
        String uuidStr = uuid.toString(); // should be a 16 byte string
        for (int i = 0; i < 16; i++) {
            objBytes[i + 8] = (byte) uuidStr.charAt(i);
        }
        //
        // Calculate CRC
        // From http://www.repairfaq.org/filipg/LINK/F_crc_v3.html
        //
        // FIXME : This might not be the right CRC alg
        //
        sun.misc.CRC16 crc = new sun.misc.CRC16();
        for (int i = 0; i < objBytes.length; i++) {
            crc.update(objBytes[i]);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("CRC={}", Integer.toHexString(crc.value));
        }
        objBytes[6] = (byte) (crc.value >> 8);
        objBytes[7] = (byte) crc.value;
        //
        // Turn it into a hex string
        //
        for (int i = 0; i < objBytes.length; i++) {
            String str = Integer.toHexString(objBytes[i]);
            if (str.length() < 2) {
                str = "0" + str;
            }
            if (str.length() > 2) { // sometimes it retuens 8 haracters paddeded with FFFF
                str = str.substring(str.length() - 2);
            }
            objID = objID + str;
            // System.out.println(i + "-"+str);
        }
        //
        return objID.toUpperCase();
    }
}
