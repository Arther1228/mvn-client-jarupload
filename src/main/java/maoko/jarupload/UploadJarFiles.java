package maoko.jarupload;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import maoko.jarupload.conf.MvnCmd;
import maoko.jarupload.sys.os.EOsType;
import maoko.jarupload.sys.os.OSPlatformUtil;

/**
 * 上传一个jar,包含多个文件
 * 
 * @dscr
 * @author fanpei
 * @time 2019年6月10日 下午5:31:11
 *
 */
public class UploadJarFiles implements Runnable {

	private static Logger logger = null;
	private static Pattern DATE_PATTERN = null;
	public static File TMP_DIR;// 文件临时根目录

	public static void init() throws Exception {
		logger = LoggerFactory.getLogger(UploadJarFiles.class);
		DATE_PATTERN = Pattern.compile("-[\\d]{8}\\.[\\d]{6}-");
		String tmpDir = null;
		EOsType ostype = OSPlatformUtil.getOSType();
		if (EOsType.Linux == ostype) {
			tmpDir = "/tmp";
		} else if (EOsType.Windows == ostype) {
			tmpDir = "C:\\tmp";
		} else {
			throw new Exception("not supperted os system!");
		}
		TMP_DIR = new File(tmpDir);
		if (!TMP_DIR.exists())
			TMP_DIR.mkdirs();
	}

	private File jarDir;

	/**
	 * @param path jar父目录
	 */
	public UploadJarFiles(File jarDir) {
		this.jarDir = jarDir;
	}

	public void run() {
		try {
			// 判断路径过长移动到临时文件夹
			File pom = null;
			File jar = null;
			File source = null;
			File javadoc = null;
			File[] files = jarDir.listFiles();
			// 忽略日期快照版本，如 xxx-mySql-2.2.6-20170714.095105-1.jar
			for (File file : files) {
				String name = file.getName();
				if (DATE_PATTERN.matcher(name).find()) {
					// skip
				} else if (name.endsWith(".pom")) {
					pom = file;
				} else if (name.endsWith("-javadoc.jar")) {
					javadoc = file;
				} else if (name.endsWith("-sources.jar")) {
					source = file;
				} else if (name.endsWith(".jar")) {
					jar = file;
				}
			}
			deploy(pom, jar, source, javadoc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 上传包
	 * 
	 * @param pom
	 * @throws IOException
	 */
	public void deploy(final File pom, final File jar, final File source, final File javadoc) throws IOException {
		String cmdStr = MvnCmd.getFullCmdStr(pom, jar, source, javadoc);
		File file = jar == null ? pom : jar;
		int result = upload(file.getAbsolutePath(), file.getParentFile(), cmdStr);
		if (result != 0) {// 重试
			logger.info("移动目录后重试上传:" + file.getAbsolutePath());
			logger.info("starting copy files to tmp dir");
			StringBuilder newdirsb = new StringBuilder(TMP_DIR.getAbsolutePath()).append(File.separator);
			String newJarPath = newdirsb.append(file.getName()).toString();
			String newdir = newJarPath.replace(".jar", "");
			File destTmp = new File(newdir);
			try {
				// 复制至临时目录
				FileUtils.copyDirectory(file.getParentFile(), destTmp);
				logger.info("sucessful copy files to tmp dir");
				upload(newJarPath, destTmp, cmdStr);
			} finally {
				// 上传完后删除
				FileUtils.deleteDirectory(destTmp);
			}
		}
	}

	/**
	 * @param jar
	 * @param cmd
	 * @return 0:成功
	 */
	private int upload(final String filePath, final File workDir, String cmdStr) {
		int result = 1;
		Process proc = null;
		try {
			logger.info("\r\n========================================================\r\n执行命令:" //
					+ cmdStr //
					+ "\r\n========================================================");
			proc = Runtime.getRuntime().exec(cmdStr, null, workDir);
			// 线程读取输出流
			TheadPoolExc.excutePrint(new StandardStreamPrint(proc.getInputStream()));
			TheadPoolExc.excutePrint(new StandardStreamPrint(proc.getErrorStream()));
			result = proc.waitFor();
			if (result != 0) {
				logger.error("上传失败:{}", filePath);
			} else
				logger.info("上传成功:{}", filePath);
			System.out.println(System.lineSeparator());
		} catch (Exception e) {
			logger.error("上传失败：{}", filePath, e);
			e.printStackTrace();
		} finally {
			if (proc != null)
				proc.destroy();// 销毁线程
		}
		return result;
	}
}
