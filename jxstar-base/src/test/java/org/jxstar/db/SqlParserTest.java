package org.jxstar.db;


import org.jxstar.dao.util.SQLParseException;
import org.jxstar.dao.util.SqlParserImp;
import org.jxstar.test.AbstractTest;

public class SqlParserTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//{JOINSTR}({JOINSTR}('{dept_id} like ''', sys_user.dept_id), '%''') as condition,
		String sql = "select "+
		 "{JOINSTR}({JOINSTR}('[dept_id] like ''',sys_user.dept_id),'%''') as condition";

		SqlParserImp parser = new SqlParserImp();
		try {
			String bb = parser.parse(sql);
			System.out.println(bb);
		} catch (SQLParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
