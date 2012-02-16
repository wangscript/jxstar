package org.jxstar.design;


import org.jxstar.fun.design.ReadDesignBO;
import org.jxstar.test.AbstractTest;

public class ReadDesignTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ReadDesignBO form  = new ReadDesignBO();
		form.readFrom("work_mal_app", "2");
	}

}
