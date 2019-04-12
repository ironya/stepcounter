package jp.sf.amateras.stepcounter;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * �e�탆�[�e�B���e�B���\�b�h��񋟂���N���X
 */
public class Util {

	private static String fileEncoding = null;
	private static FileEncodingDetector fileEncodingDetector = null;

	public static final String IGNORE_GENERATED_FILE = "ignore.generated.file";
	public static final String EXTENSION_PAIRS = "extension.pairs";
	public static final String IGNORE_FILENAME_PATTERNS = "ignore.filename.patterns";
	public static final String FILENAME_PATTERNS = "filename.patterns";
	public static final String EXTENSION_SEPARATOR = ",";
	public static final String PAIR_SEPARATOR = "|";
	public static final String PAIR_SEPARATOR_REGEX = "\\|";
	public static final String FILENAME_PATTERN_SEPARATOR = "|||";
	public static final String FILENAME_PATTERN_SEPARATOR_REGEX = "\\|\\|\\|";
	
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
	
	public static String[] exceptGeneratedFile(String[] filePaths) {
		return exceptGeneratedFile(null, filePaths);
	}
	
	public static File[] exceptGeneratedFile(File[] files) {
		return exceptGeneratedFile(null, files);
	}
	
	public static String[] exceptGeneratedFile(String basePath, String[] filePaths) {
		if (!Boolean.getBoolean(IGNORE_GENERATED_FILE)) {
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
		File[] exceptedFiles = exceptGeneratedFile(basePathFile, files);
		String[] exceptedPaths = new String[exceptedFiles.length];
		for (int i=0; i<exceptedPaths.length; i++) {
			exceptedPaths[i] = exceptedFiles[i].getPath();
		}
		return exceptedPaths;
	}
	
	public static Map<String, String> createExtensionPairs() {
		Map<String, String> pairs = new LinkedHashMap<String, String>();
		String source = System.getProperty(EXTENSION_PAIRS);
		if (source == null || source.length() == 0) {
			return pairs;
		}
		String[] rows = source.split(PAIR_SEPARATOR_REGEX);
		for(String row : rows) {
			String[] values = row.split(EXTENSION_SEPARATOR);
			if (values.length >= 2) {
				pairs.put(values[0], values[1]);
			}
		}
		return pairs;
	}

	public static boolean ignoreFilenamePatterns() {
		return Boolean.getBoolean(IGNORE_FILENAME_PATTERNS);
	}
	
	public static boolean ignoreGeneratedFile() {
		return Boolean.getBoolean(IGNORE_GENERATED_FILE);
	}
	
	public static String getExtensionPairsString() {
		return System.getProperty(EXTENSION_PAIRS);
	}
	
	public static String getFilenamePatternsString() {
		return System.getProperty(FILENAME_PATTERNS);
	}
	
	public static File[] exceptGeneratedFile(File basePathFile, File[] files) {
		if (!ignoreGeneratedFile()) {
			return files;
		}
		if (files == null) {
			return files;
		}
		Map<String, String> extensionPairs = createExtensionPairs();
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
	
	public static boolean matchToAny(Pattern[] patterns, File parent, File file) {
		if (!Util.ignoreFilenamePatterns()) {
			return false;
		}
		File target = parent == null ? file : new File(parent, file.getPath());
		if (!target.isFile()) {
			return false;
		}
		String path;
		try {
			path = target.getCanonicalPath();
		} catch (IOException e) {
			path = target.getAbsolutePath();
		}
		return matchToAny(patterns, path);
	}
	
	public static boolean matchToAny(Pattern[] patterns, String value) {
		for(Pattern pattern : patterns) {
			if (pattern.matcher(value).matches()) {
				return true;
			}
		}
		return false;
	}
	
	public static Pattern[] createFilenamePatterns() {
		String filenamePatternsString = System.getProperty(FILENAME_PATTERNS);
		if (filenamePatternsString == null) {
			return new Pattern[0];
		}
		String[] filenamePatterns = filenamePatternsString.split(FILENAME_PATTERN_SEPARATOR_REGEX);
		return createPatterns(filenamePatterns);
	}
	
	public static Pattern[] createPatterns(String[] patternStrings) {
		if (patternStrings == null) 
			return new Pattern[0];
		if (patternStrings.length == 1 && patternStrings[0].trim().length() == 0) {
			return new Pattern[0];
		}
		List<Pattern> patterns = new ArrayList<Pattern>(patternStrings.length);
		for(String regex : patternStrings) {
			try {
				patterns.add(Pattern.compile(regex));
			} catch (PatternSyntaxException e) {
				System.err.println("Ignore regex pattern:\"" + regex + "\" : cause by exception as follows");
				e.printStackTrace();
			}
		}
		return patterns.toArray(new Pattern[patterns.size()]);
	}
}
