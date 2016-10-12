package jp.sf.amateras.stepcounter;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * �e�탆�[�e�B���e�B���\�b�h��񋟂���N���X
 */
public class Util {

	private static String fileEncoding = null;
	private static FileEncodingDetector fileEncodingDetector = null;

	/**
	 * ��������w�蕶����ŕ������A�z��ŕԋp���܂��B
	 *
	 * @param str ������
	 * @param del ��؂蕶����
	 * @return �������ꂽ��������i�[�����z��
	 */
	public static String[] split(String str,String del){
		ArrayList<String> list = new ArrayList<String>();
		int pos   = 0;
		int index = 0;
		while((index=str.indexOf(del,pos))!=-1){
			list.add(str.substring(pos,index));
			pos = index + del.length();
		}
		list.add(str.substring(pos,str.length()));
		return (String[])list.toArray(new String[list.size()]);
	}

	/**
	 * �n���ꂽ��������w��G���R�[�f�B���O�̎w��o�C�g���Ő擪����؂�o���B
	 * �J�^�J�i�̔���͐������s�����Ƃ��ł��Ȃ��B
	 *
	 * @param   s    �؂�o���Ώە�����
	 * @param   cnt  �؂�o���o�C�g��
	 * @return  ���ʕ�����
	 */
	public static String substring(String str,int length){
		String resultStr = null;
		int zenCnt      = 0;
//		int kisuuFlg    = 0;
		int loopCnt     = length;
		byte[] resBytes = new byte[length];
		byte[] bytes    = str.getBytes();
		// �w��o�C�g���ȉ��̏ꍇ�͂��̂܂ܕԋp
		if(bytes.length <= length) {
			return str;
		}
		for (int i=0; i < length; i++) {
			if (bytes[i] < 0) {
				// bytes[i]��8�r�b�g�ڂ������Ă���(�S�p)
				zenCnt ++;
			}
		}
		// �S�p�o�C�g�̐�����̏ꍇ
		if(zenCnt % 2 == 1) {
			loopCnt--;
		}
		for(int i=0; i < loopCnt ; i++) {
			resBytes[i] = bytes[i];
		}
		resultStr = new String(resBytes);
		return resultStr;
	}

	/**
	 * �����œn����������̃o�C�g����Ԃ��܂��B
	 *
	 * @param str ������
	 * @return �o�C�g��
	 */
	public static int getByteLength(String str){
		try {
			byte[] bytes = str.getBytes();
			return bytes.length;
		} catch(Exception ex){
			return str.getBytes().length;
		}
	}

	/**
	 * HTML/XML�̓��ꕶ�������ԎQ�Ƃɕϊ����܂��B
	 *
	 * @param str ������
	 * @return �ϊ���̕�����
	 */
	public static String escapeXML(String str){
		str.replaceAll("&" ,"&amp;");
		str.replaceAll("<" ,"&gt;");
		str.replaceAll(">" ,"&lt;");
		str.replaceAll("\"","&quot;");
		return str;
	}

	/**
	 * �X�g���[���������I�ɃN���[�Y���܂��B
	 *
	 * @param closeable �X�g���[��
	 */
	public static void close(Closeable closeable){
		if(closeable != null){
			try {
				closeable.close();
			} catch(Exception ex){
				;
			}
		}
	}

	public static void setFileEncodingDetector(FileEncodingDetector detector){
		fileEncodingDetector = detector;
	}

	public static void setFileEncoding(String encoding){
		fileEncoding = encoding;
	}

	public static String getFileEncoding(File file){
		if(fileEncoding != null){
			return fileEncoding;
		}

		if(fileEncodingDetector != null){
			String encoding = fileEncodingDetector.getEncoding(file);
			if(encoding != null){
				return encoding;
			}
		}

		return System.getProperty("file.encoding");
	}
	
	public static final Map<String, String> PRIOR_EXTENSION_PAIRS;
	static {
		PRIOR_EXTENSION_PAIRS = new LinkedHashMap<String, String>();
		PRIOR_EXTENSION_PAIRS.put(".sqlj", ".java");
	}
	
	public static String[] exceptGeneratedFile(String[] filePaths) {
		return exceptGeneratedFile(PRIOR_EXTENSION_PAIRS, null, filePaths);
	}
	
	public static String[] exceptGeneratedFile(String basePath, String[] filePaths) {
		return exceptGeneratedFile(PRIOR_EXTENSION_PAIRS, basePath, filePaths);
	}
	
	public static File[] exceptGeneratedFile(File[] files) {
		return exceptGeneratedFile(PRIOR_EXTENSION_PAIRS, null, files);
	}
	
	public static String[] exceptGeneratedFile(Map<String, String> extensionPairs, String basePath, String[] filePaths) {
		if (!Boolean.getBoolean("ignore.generated.file")) {
			return filePaths;
		}
		if (filePaths == null) {
			return filePaths;
		}
		File[] files = new File[filePaths.length];
		for(int i=0; i<files.length; i++) {
			files[i] = new File(filePaths[i]);
		}
		File basePathFile = basePath == null ? null : new File(basePath);
		File[] exceptedFiles = exceptGeneratedFile(extensionPairs, basePathFile, files);
		String[] exceptedPaths = new String[exceptedFiles.length];
		for (int i=0; i<exceptedPaths.length; i++) {
			exceptedPaths[i] = exceptedFiles[i].getPath();
		}
		return exceptedPaths;
	}
	
	public static File[] exceptGeneratedFile(Map<String, String> extensionPairs, File basePathFile, File[] files) {
		if (!Boolean.getBoolean("ignore.generated.file")) {
			return files;
		}
		if (files == null) {
			return files;
		}
		List<File> fileList = new ArrayList<File>(Arrays.asList(files));
		List<File> priors = gatherPriorExtensionFiles(extensionPairs, basePathFile, files);
		for (Iterator<File> itr = fileList.iterator(); itr.hasNext();) {
			File file = itr.next();
			if (!(basePathFile == null ? file.isFile() : new File(basePathFile, file.getPath()).isFile())) {
				continue;
			}
			String fileExtension = selectExtension(file);
			if (extensionPairs.containsValue(fileExtension)) {
				String filePath = file.getPath();
				String filePathWithoutExtension = filePath.substring(0, filePath.length() - fileExtension.length());
				for (Entry<String, String> extensionPair : extensionPairs.entrySet()) {
					if (!fileExtension.equals(extensionPair.getValue())) {
						continue;
					}
					if (priors.contains(new File(filePathWithoutExtension + extensionPair.getKey()))) {
						itr.remove();
						break;
					}
				}
			}
		}
		return fileList.toArray(new File[fileList.size()]);
	}

	private static List<File> gatherPriorExtensionFiles(Map<String, String> extensionPairs, File basePathFile, File[] files) {
		List<File> priors = new ArrayList<File>();
		for(File file : files) {
			if (basePathFile == null ? file.isFile() : new File(basePathFile, file.getPath()).isFile()) {
				String extension = selectExtension(file);
				if (extensionPairs.containsKey(extension)) {
					priors.add(file);
				}
			}
		}
		return priors;
	}

	private static String selectExtension(File file) {
		String path = file.getPath();
		int lastPeriodIndex = path.lastIndexOf('.');
		return lastPeriodIndex > 0 ? path.substring(lastPeriodIndex) : "";
	}
}
