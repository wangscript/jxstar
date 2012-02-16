/*
 * Xls2Html.java 2010-11-12
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;
/*
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
*/
/**
 * 
 *
 * @author TonyTan
 * @version 1.0, 2010-11-12
 */
public class Xls2Html {
    /**  
     * EXCEL转成HTML  
     * @param xlsfile EXCEL文件全路径  
     * @param htmlfile 转换后HTML存放路径  
     */  
    public static void excelToHtml(String excelPath, String htmlPath)   
    {   /*
        ActiveXComponent offCom = new ActiveXComponent("Excel.Application"); 
        try  
        {   
         offCom.setProperty("Visible", new Variant(false));   
            Dispatch excels = offCom.getProperty("Workbooks").toDispatch();   
            Dispatch excel = Dispatch.invoke(excels, 
             "Open",Dispatch.Method, new Object[] { excelPath, new Variant(false),  new Variant(true) }, new int[1]).toDispatch();   
            Dispatch.invoke(excel, "SaveAs", Dispatch.Method, new Object[] {   
              htmlPath, new Variant(44) }, new int[1]);   
            Variant f = new Variant(false);   
            Dispatch.call(excel, "Close", f);   
        }   
        catch (Exception e)   
        {   
            e.printStackTrace();   
        }   
        finally  
        {   
         offCom.invoke("Quit", new Variant[] {});   
        }   */
    }    
    
    
    public static void main(String[] args)
 {
    	//java.library.path
    	System.out.println(System.getProperty("user.dir"));

    	System.out.println(System.getProperty("java.library.path"));


     //excelToHtml("c:/ftc/kk.xls","c:/ftc/kk.html");
     //wordToHtml("c:/ftc/gdf.doc","c:/ftc/gdf.html");
    	excelToHtml("d:/bb.xls","d:/bb.html");
 }

}
