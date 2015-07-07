package net.hakugyokurou.fgow.plugin;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;

import com.google.common.io.Files;

class ActionCleanClassic extends ActionMD5 {
	private File[][] deletedFolder;
	
	public ActionCleanClassic(File[] ... deletedFolder) {
		this.deletedFolder = deletedFolder;
	}
	
	@Override
	public void execute(Object arg0) {
		for(int i=0;i<deletedFolder.length;i++)
		{
			md5s = getRecord(deletedFolder[i][1]);
			cleanFiles(deletedFolder[i][0].getAbsolutePath().length()+1, deletedFolder[i][0], false);
			deletedFolder[i][1].delete();
		}
	}
	
	private void cleanFiles(int basePathLength, File folder, boolean deleteFolderIfEmpty) {
		File[] files = folder.listFiles();
		for (File file : files) {
			if(file.isDirectory())
				cleanFiles(basePathLength, file, true);
			else 
			{
				String relativePath = file.getAbsolutePath().substring(basePathLength).replace('\\', '/');
				String oldMd5 = md5s.get(relativePath);
				if(oldMd5 != null)
				{
					String nowMd5 = getMD5(file);
					//System.out.println(relativePath+": OMD5 - "+oldMd5 + " NMD5 - "+nowMd5);
					if(nowMd5.equals(oldMd5))
						file.delete();
				}
			}
		}
		if(deleteFolderIfEmpty)
		{
			files = folder.listFiles();
			if(files.length==0)
				folder.delete();
		}
	}
	
	private HashMap<String, String> getRecord(File file) {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		if(file.exists())
		{
			try {
				List<String> lines = Files.readLines(file, Charset.defaultCharset());
				for(String line : lines)
				{
					if(line.trim().isEmpty())
						continue;
					hashMap.put(line.substring(0, line.lastIndexOf(':')), 
							line.substring(line.lastIndexOf(':')+1, line.length()-1));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return hashMap;
		}
		else
		{
			throw new RuntimeException("Can't find file:"+file.getAbsolutePath());
		}
	}
}
