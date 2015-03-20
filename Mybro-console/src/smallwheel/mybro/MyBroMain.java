package smallwheel.mybro;
import java.sql.Connection;

import org.apache.log4j.Logger;

import smallwheel.mybro.builder.dto.DtoClassBuilder;
import smallwheel.mybro.common.DBManager;
import smallwheel.mybro.common.ENV;
import smallwheel.mybro.support.SqlMapperBuilderFactory;
import smallwheel.mybro.support.builder.SqlMapperBuilder;

/**
 * MyBro ���� Ŭ����
 * 
 * @author yeonhooo
 * 
 * TODO: Connection ����
 * TODO: ��Ű�� ��� ���� �Է¹޾�, ��Ű�� ���� �� �ش� ��Ű�� ��ο� ���� �߰�
 * TODO: Mapper.java ���� ���� ��� 
 *
 */
public class MyBroMain {
	
	private final static Logger LOGGER = Logger.getLogger(MyBroMain.class);
	
	private DBManager dbm;
	private Connection con;
	private SqlMapperBuilder sqlMapperBuilder;
	private DtoClassBuilder dtoClassBuilder;

	public static void main(String[] args) {
		MyBroMain main = new MyBroMain();
		main.run();
	}

	private void init() {
		// ȯ���ʱ�ȭ , ȯ�����Ͽ��� ������ �ε�
		ENV.init();

		// Ÿ�Ժ�(ibatis, mybatis) sqlMapperBuilder ����
		SqlMapperBuilderFactory factory = new SqlMapperBuilderFactory();
		sqlMapperBuilder = factory.createSqlMapperBuilder(ENV.mapperType);

		// DB ����
		dbm = new DBManager();
		dbm.checkConnection(ENV.dbms);
		con = dbm.getConnection(ENV.dbms);

		// dtoClassBuilder ����
		dtoClassBuilder = new DtoClassBuilder();
	}

	private void run() {
		
		init();
		
		// DAO Ŭ������ SqlMap.xml ������ ������� ���̺� ���
		final String[] TABLE_LIST = { 
				"city"
				, "country"
		};

		final String[] PROCEDURE_LIST = {
		// "EXEC [PROC_BB_KBO_APP_SDMS_MOBILE_VOTE_CNT_S]"
		// "PROC_BB_KBO_XMLMAKER_STATS_LIVEPLAYER_BATTER_S '2010', '20100504HHHT0'"
		};

		// ���̺� ���� Ŭ���� ���ϰ� XML ���� ����
		for (int i = 0; i < TABLE_LIST.length; i++) {
			// step1. �� Ŭ���� ������ �����Ѵ�.
			dtoClassBuilder.makeModelClassFileByTable(i, con, TABLE_LIST[i], ENV.prefixExcept);

			// step2. SqlMap.xml ������ �����Ѵ�.
			sqlMapperBuilder.writeSqlMap(TABLE_LIST[i]);
		}

		// ���ν����� ���� Ŭ���� ���ϰ� XML ���� ����
		for (int i = 0; i < PROCEDURE_LIST.length; i++) {
			// step1. �� Ŭ���� ������ �����Ѵ�.
			dtoClassBuilder.makeModelClassFileByProcedure(i, con, PROCEDURE_LIST[i]);

			// step2. SqlMap.xml ������ �����Ѵ�.
			sqlMapperBuilder.writeSqlMap(null);
		}
		
		LOGGER.info("### " + TABLE_LIST.length + "�� ���̺� ���� �۾��� �Ϸ�Ǿ����ϴ�." );
	}
}
