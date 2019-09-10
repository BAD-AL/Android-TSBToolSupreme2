package com.example.tsbtoolsupreme;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;


/// <summary>
    /// Summary description for TecmoToolFactory.
    /// </summary>
    public class TecmoToolFactory
    {
        public static ITecmoTool GetToolForRom(InputStream stream, String fileName) throws IOException
        {
            ITecmoTool tool = null;
            ROM_TYPE type = ROM_TYPE.NONE;
            byte[] romBytes = readAllBytes(stream);
            type = CheckRomType(romBytes,  fileName);
            if( type == ROM_TYPE.CXROM ) {
                tool = new CXRomTSBTool();
                tool.Init(romBytes);
                TecmoTool.Teams = new String[] {
                        "bills",     "dolphins", "patriots", "jets",
                        "bengals",    "browns",  "ravens",   "steelers",
                        "colts",      "texans",  "jaguars",  "titans",
                        "broncos",    "chiefs",  "raiders",  "chargers",  
                        "redskins",   "giants",  "eagles",   "cowboys",
                        "bears",      "lions",   "packers",  "vikings",   
                        "buccaneers", "saints",  "falcons",  "panthers",
                         
                        "AFC",     "NFC",
                        "49ers",   "rams", "seahawks",   "cardinals"
                    };
                
            }
            else if( type == ROM_TYPE.SNES ) {
                TecmoTool.Teams = new String[] {
                "bills",   "colts",  "dolphins", "patriots",  "jets",
                "bengals", "browns", "oilers",   "steelers",
                "broncos", "chiefs", "raiders",  "chargers",  "seahawks",
                "cowboys", "giants", "eagles",   "cardinals", "redskins",
                "bears",   "lions",  "packers",  "vikings",   "buccaneers",
                "falcons", "rams",   "saints",   "49ers"
                  };
                tool = new SNES_TecmoTool();
                tool.Init(romBytes);
            }
            else
            {
                tool = new TecmoTool();
                tool.Init(romBytes);
                TecmoTool.Teams = new String[] 
                    {
                        "bills",   "colts",  "dolphins", "patriots",  "jets",
                        "bengals", "browns", "oilers",   "steelers",
                        "broncos", "chiefs", "raiders",  "chargers",  "seahawks",
                        "redskins","giants", "eagles",   "cardinals", "cowboys",
                        "bears",   "lions",  "packers",  "vikings",   "buccaneers",
                        "49ers",   "rams",   "saints",   "falcons"
                    };
            }

            return tool;
        }

        /// <summary>
        /// returns 0 if regular NES TSB rom
        ///         1 if it's cxrom TSBROM type.
        /// Throws exceptions (UnauthorizedAccessException and others)
        /// </summary>
        /// <param name="fileName"></param>
        /// <returns></returns>
        public static ROM_TYPE CheckRomType( byte[] romBytes, String fileName )
        {
            ROM_TYPE ret = ROM_TYPE.NES;
            if( romBytes != null && romBytes.length > 0x99 && romBytes[0x48] == (byte)0xff ){
                //                    if( fileName.ToLower().EndsWith(".nes") && len > 0x70000 ) //cxrom size=0x80010
                ret = ROM_TYPE.CXROM;
            }
            else if( fileName.toLowerCase().endsWith(".smc")) {
                ret = ROM_TYPE.SNES;
            }
            return ret;
        }

        /**
         * Returns a byte array
         * @param stream the input stream to read from.
         * @return byte array, 2MB max
         * @throws IOException
         */
        public static byte[] readAllBytes(InputStream stream) throws IOException
        {
            // allocate a 2MB array (SNES rom is 1,536 KB)
            byte[] buffer = new byte[1024*1024*2];
            int len = stream.read(buffer);
            stream.close();
            byte[] retVal = new byte[len];
            System.arraycopy(buffer,0,retVal,0,len);
            return  retVal;
        }
    }
    