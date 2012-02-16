package org.jxstar.util;

import java.io.File;
import java.io.FilenameFilter;
import java.text.MessageFormat;

/**
 * 批量把文件编码由GBK转为UTF8，可以继续完善做成在命令行中执行的程序，
 * 可以添加文件名过滤等功能，暂时未实现。
 *
 * @author TonyTan
 * @version 1.0, 2011-11-25
 */
public class FileUTF8GBK {
    public static void main(String[] args) {
        //需要转换的文件目录
        String fromPath = "D:\\tomcat6\\webapps\\pm\\jxstar";
        //转换到指定的文件目录
        String toPath = "D:\\tmp";
        
        info("start transform [from path]={0} [to path]={1}", fromPath, toPath);
        
        //递归取到所有的文件进行转换
        transform(fromPath, toPath, new ExtFilter("inc"));
    }
    
    /**
     * 把一个目录中的文件转换到另一个目录中
     * @param fromPath -- 来源文件目录
     * @param toPath -- 目标文件目录
     * @return
     */
    public static boolean transform(String fromPath, String toPath, FilenameFilter filter) {
        File ftmp = new File(fromPath);
        if (!ftmp.exists()) {
            info("转换文件路径错误！");
            return false;
        }
        
        info("frompath is [{0}], topath is [{1}]", fromPath, toPath);
        
        //如果是文件，则转换，结束
        if (ftmp.isFile()) {
        	byte[] value = FileUtil.fileToBytes(fromPath);
            String content = StringUtil.convEncoding(value, "GBK", "UTF-8");
            return FileUtil.saveFileUtf8(toPath, content);
        } else {
            //查找目录下面的所有文件与文件夹
            File[] childFiles = ftmp.listFiles(filter);
            for (int i = 0, n = childFiles.length; i < n; i++) {
                File child = childFiles[i];
                String childFrom = fromPath + "/" + child.getName();
                String childTo = toPath + "/" + child.getName();
                
                transform(childFrom, childTo, filter);
            }
        }
        
        return true;
    }
    
    //扩展文件名过滤
    private static class ExtFilter implements FilenameFilter {
    	private String extName = "";
    	
    	public ExtFilter(String extName) {
    			this.extName = "." + extName;
    	}
    	
    	public boolean accept(File dir, String name) {
    		File f = new File(dir.getPath() + "\\" + name);
    		if (f.isHidden()) return false;
    		if (f.isDirectory()) return true;
    		
    		return name.endsWith(extName);
    	}
    }
    
    private static void info(String message, Object... params) {
        message = MessageFormat.format(message, params);
        
        System.out.println(message);
    }
}
