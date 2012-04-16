package org.jxstar.util;

import java.io.File; 
import java.io.FileReader; 
import java.io.FileWriter; 
import java.io.FilenameFilter; 
import java.io.IOException; 
import java.util.ArrayList; 
import java.util.Iterator; 
import java.util.List; 
import java.util.regex.Pattern;

/** 
 * @author Newweapon @ ustc [Email: newweapon111 (at) gmail.com] 
 * @version 0.0.1 
 * @Created 20071015 
 *  
 * @TODO 1. user can specify the formatted file name;<br/> 
 *         2. User can located the formatted files to a specified folder.<br/>  
 *         3. If a file is formatted partly already. Delete all blank characters first, and then format the file.<br/> 
 */ 
public class JsFormatter { 
     
    /* ============ constants begin =================== */ 
    /** Usage */ 
    public static String USAGE = "Usage:  JsFormatter -d path/directory  JsFormatter -f path/filename.js"; 
     
    /** Type: Directory = "0"; File = "1" */ 
    public interface Type { 
        /** Directory: 0 */ 
        public static String DIRECTORY = "0"; 
        /** File: 1 */ 
        public static String FILE = "1"; 
    } 
    /* ============ constants end =================== */ 
     
    /** 
     * Entry point of the project. 
     *  
     * @param args Like "-d /path/directory" or "-f /path/file.js" 
     */ 
    public static void main(String[] args) { 
        String startMsg = "Processing..."; 
        String finishMsg = "Finished.";
             
        // Parameters check.  
        if(args.length != 2) { 
            System.err.println(USAGE); 
            return; 
        } 
        // Get the two parameters. 
        String type = args[0]; 
        String path = args[1]; 
        // Parameters check. 
        if("-d".equals(type) || "--directory".equals(type)) { 
            type = Type.DIRECTORY; 
        } else if("-f".equals(type) || "--file".equals(type)) { 
            type = Type.FILE; 
        } else { 
            System.err.println(USAGE); 
            return; 
        } 
        // Check file type  
        if(Type.FILE.equals(type)) { 
            if(path.length() <= 3 || !isJsFile(path)) { 
                System.err.println("The file must be a JS file."); 
                return; 
            } 
        } 
         
        // Start message 
        System.out.println(startMsg); 
         
        // Format file(s) 
        try { 
            if(Type.FILE.equals(type)) { 
                formatFile(path); 
            } else { 
                List<String> jsFileList = getJsFileList(path); 
                Iterator<String> it = jsFileList.iterator(); 
                while(it.hasNext()) { 
                    formatFile(it.next()); 
                } 
                finishMsg += " (" + jsFileList.size() + " file(s) formatted)"; 
            } 
        } catch (Exception e) { 
            e.printStackTrace(); 
            return; 
        } 
         
        // Finish message 
        System.out.println(finishMsg); 
    } 

    /**  
     * Format a JS file. 
     *  
     * @param fileName The file name of the file which is to be formatted. 
     * @return String The formatted string. 
     * @throws IOException Exception when open, read and write file. 
     */ 
    private static void formatFile(String fileName) throws IOException { 
        String formattedFileName = fileName + ".formatted"; 
         
        FileReader fr = new FileReader(fileName); 
        FileWriter fw = new FileWriter(formattedFileName); 
         
        String lastWord = ""; 
         
        int forCount = 0; 
        int quoteCount = 0; 
        int sigleQuoteCount = 0; 
        int bracketCount = 0; 
         
        int thisChar = 0; 
        int lastChar = 0; 
        int nextChar = 0; 
         
        thisChar = fr.read(); 
        if(thisChar != -1) { 
            nextChar = fr.read(); 
        } 
         
        while(thisChar != -1) { 
            // find and replace 
            switch(thisChar) { 
            // 2. add   after ";" (Except "for", and ";" between " " which is part of a string in javascript. ) and   before the next line 
                case ';': 
                    // If the ";" is in quote or in "for", then not print " " 
                    if(quoteCount > 0 || sigleQuoteCount > 0 || forCount > 0) { 
                        fw.write(';'); 
                        if(forCount > 0) { 
                            forCount--; 
                        } 
                    // Add " " after ";" 
                    } else { 
                        fw.write(';'); 
                        if(' ' != nextChar && ' ' != nextChar) { 
                            fw.write(' '); 
                            fillTableChar(fw, bracketCount); 
                        } 
                    } 
                    break; 
                case '{': // 3. add " " and " " after "{" 
                    bracketCount++; 
                    fw.write('{'); 
                    if(' ' != nextChar && ' ' != nextChar) { // If the file is already formatted, don't add   after {. 
                        fw.write(' '); 
                        fillTableChar(fw, bracketCount); 
                    } 
                    break; 
                case '}': // 4. add " " and " " before "}" 
                    bracketCount--; 
                    fw.write(' '); 
                    fillTableChar(fw, bracketCount); 
                    fw.write('}'); 
                    if(';' != nextChar && '}' != nextChar && ' ' != nextChar && ' ' != nextChar) { 
                        fw.write(' '); 
                        fillTableChar(fw, bracketCount); 
                    } 
                    break; 
                case '\'': 
                    fw.write('\''); 
                    if(quoteCount == 0) { //When ' is not between "", change its state.  
                        sigleQuoteCount = sigleQuoteCount == 0 ? 1 : 0; 
                    } 
                    break; 
                case '"': 
                    fw.write('"'); 
                    if(sigleQuoteCount == 0) { //When ' is not between "", change its state. 
                        quoteCount = quoteCount == 0 ? 1 :0; 
                    } 
                    break; 
                case 'f': // 1. add   before "function" 
                    if(nextChar == 'u' && lastChar != '=') { // TODO This is a very weak way to determine whether this coming word is "function", so it is need to be fixed. 
                        fw.write(' '); 
                        fw.write(' '); 
                    } 
                    fw.write('f'); 
                    break; 
                default: 
                    fw.write(thisChar); 
                    break; 
            } 
             
            if(isAlpha(thisChar)) { 
                if(!isAlpha(lastChar)) { 
                    lastWord = ""; 
                } 
                lastWord += String.valueOf(thisChar); 
            } else { 
                if(isAlpha(lastChar)) { 
                    if("102111114".equals(lastWord)) { // "for" 
                        forCount = 2; 
                    } 
                    //TODO Whether is is suitable here to determine "function" and add " " before it? 
                } else { 
                    lastWord = String.valueOf(thisChar); 
                } 
            } 
             
            lastChar = thisChar; 
            thisChar = nextChar; 
            if(thisChar != -1) { 
                nextChar = fr.read(); 
            } 
        } 
         
        // close the files 
        fw.close(); 
        fr.close(); 
    } 
     
    /** 
     * Find all JS files in the specified directory. 
     *  
     * @param directory The directory in which the files to be listed.  
     * @return List<String> The JS file list. 
     */ 
    private static List<String> getJsFileList(String directory) { 
        List<String>  jsFileList = new ArrayList<String>(); 
        list(directory, jsFileList); 
        return jsFileList; 
    } 
     
    /** 
     * List all the JS files in the specified directory recursively. 
     *  
     * @param path The path to be recursively searched for JS files. 
     * @param result The path and file list 
     */ 
    private static void list(String path, List<String> result) { 
        File f = new File(path); 
        if(f.isDirectory()) { 
            File[] fileList = f.listFiles(); 
            for(int i = 0; i < fileList.length; i++) { 
                list(fileList[i].getPath(), result); 
            } 
        } else { 
            if(isJsFile(f.getName())) { 
                result.add(f.getPath()); 
            } 
        } 
    } 
     
    /** 
     * Determine whether the the specified file is a JS file. 
     *  
     * @param fileName 
     * @return True: is a JS file; False: not a JS file. 
     */ 
    private static boolean isJsFile(String fileName) { 
        //TODO use pattern!!! 
        return ".js".equals(fileName.substring(fileName.length() - 3)); 
    } 
     
    /** 
     * List all JS files in the specified directory(Not in their sub-directory).  
     *  
     * @param dir The specified directory. 
     * @return String[] The JS file list. 
     */ 
    public static String[] getSingleDirJsFileList(final String dir) { 
        String[] jsFileList; 
        File path = new File(dir); 
        jsFileList = path.list(new FilenameFilter() { 
            private Pattern pattern = Pattern.compile(".js"); 
            public boolean accept(File dir, String name) { 
                return pattern.matcher(new File(name).getName()).matches(); 
            } 
        }); 
        return jsFileList; 
    } 
     
    /**  
     * Check whether the character is an alpha char. 
     * <b><red>Actually, the words exist in a function name would not be limit among those we list below.  
     * This need to be fixed. </red></b> 
     *  
     * @param c The char to be checked. 
     * @return boolean True: is alpha char; False: is not alpha char. 
     */ 
    private static boolean isAlpha(int c) { 
        return ((c > 'a' && c < 'z') || (c > 'A' && c < 'Z') || (c > '0' && c < '9')); 
    } 
     
    /** 
     * Fill specified number of ' ' 
     *  
     * @param fw        FileWriter 
     * @param charNum    Specified number of ' ' 
     * @throws IOException Exception when writing file 
     */ 
    private static void fillTableChar(FileWriter fw, int charNum) throws IOException { 
        for(int i = 0; i < charNum; i++) { 
            fw.write(' '); 
        } 
    } 
} 