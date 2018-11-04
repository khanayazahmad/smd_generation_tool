package com.btpb.gen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class RAReplicator {
	
	private Integer MODE;
	private String destinationPath;
	private String sourcePath;
	private String RANDOM_ADDRESS_INF1;
	private String RANDOM_ADDRESS_INF2;
	private Integer sasraCounter = 11;
	private Integer sraCounter = 11;
	public 	ArrayList<String> ignoreCollection;
	
	public RAReplicator(int mode){
		this.MODE = mode;
		ignoreCollection = new ArrayList<String>();
	}
	
	public Integer getMODE() {
		return MODE;
	}

	public void setMODE(Integer mODE) {
		MODE = mODE;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getRANDOM_ADDRESS_INF1() {
		return RANDOM_ADDRESS_INF1;
	}

	public void setRANDOM_ADDRESS_INF1(String rANDOM_ADDRESS_INF1) {
		RANDOM_ADDRESS_INF1 = rANDOM_ADDRESS_INF1;
	}

	public String getRANDOM_ADDRESS_INF2() {
		return RANDOM_ADDRESS_INF2;
	}

	public void setRANDOM_ADDRESS_INF2(String rANDOM_ADDRESS_INF2) {
		RANDOM_ADDRESS_INF2 = rANDOM_ADDRESS_INF2;
	}

	public Integer getSasraCounter() {
		return sasraCounter;
	}

	public void setSasraCounter(Integer sasraCounter) {
		this.sasraCounter = sasraCounter;
	}

	public Integer getSraCounter() {
		return sraCounter;
	}

	public void setSraCounter(Integer sraCounter) {
		this.sraCounter = sraCounter;
	}

	

	File generateNewSMD(File smd, HashMap<String, HashMap<String, String>> changes) {

		String line;
		int lineno = 0;
		String space="	";
		try {
				BufferedReader brsmd = new BufferedReader(new FileReader(smd));
				lineno = 0;
				ArrayList<String> smdlines = new ArrayList<String>(); 
				while((line=brsmd.readLine())!=null){
					smdlines.add(line);
				}
				
				for(String action:changes.keySet()){

					for(int i=0;i<smdlines.size();i++){
						line=smdlines.get(i);

						if(line.trim().startsWith("%")) continue;
						String own_address,peer_address;
						
						if(line.contains(action)){
							
							int j=i;
							if(line.contains("CONSTRUCT")){
								while(!smdlines.get(++j).contains(action));
							}

							if(smdlines.get(j).contains("INF_1")){
								own_address = RANDOM_ADDRESS_INF1;
								peer_address = RANDOM_ADDRESS_INF2;
							}
							else{
								own_address = RANDOM_ADDRESS_INF2;
								peer_address = RANDOM_ADDRESS_INF1;
								
							}
							if(line.contains("CONSTRUCT")){
								for(String tag:changes.get(action).keySet()){

									lineno=i;
									smdlines.get(lineno);
									String variable = tag.toUpperCase();
									String value = changes.get(action).get(tag);
									
									if(value.equals("O_ADDRESS"))
										value = own_address;
									else if(value.equals("P_ADDRESS"))
										value = peer_address;

									int flag = 0;
									while(++lineno<smdlines.size()&&!smdlines.get(lineno).contains(action)){
										if(smdlines.get(lineno).contains(tag))
											variable = smdlines.get(lineno).split("=")[1].trim();
									}
									lineno=i;
									while(--lineno>=0&&(line=smdlines.get(lineno))!=null&&!line.contains("CONSTRUCT")){
										if(line.contains(variable)&&line.indexOf(variable)<line.indexOf("=")){
											line = line.split(variable)[0]+variable+"	=	\""+value+"\"";
											smdlines.set(lineno, line);
											flag=1;
											break;
										}
									}
									if(flag == 0){
										smdlines.add(i,space+variable+"	=	\""+value+"\"");
										i++;
										smdlines.add(++i,space+tag+"	=	"+variable+"\n");
										i--;
									}
									
								}

								while(!(smdlines.get(++i).contains(action)));
							}else{

								for(String tag:changes.get(action).keySet()){

									String variable = tag.toUpperCase();
									String value = changes.get(action).get(tag);
									if(value.equals("O_ADDRESS"))
										value = own_address;
									else if(value.equals("P_ADDRESS"))
										value = peer_address;
									smdlines.add(i++,space+variable+"	=	\""+value+"\"");
								}
								smdlines.add(i++,space+"CONSTRUCT,"+line.split(",")[1]+","+line.split(",")[2]+", , , , ,");
								for(String tag:changes.get(action).keySet()){
									String variable = tag.toUpperCase();
									smdlines.add(i++,space+tag+"	=	"+variable+"\n");
								}
								
							}
							
						}
					}
					
				}
				
				String newContent = "";
				
				for(String s:smdlines){
					newContent += s + "\r\n";
				}
				String newName = getNewName(smd.getName());
				
				
				
				File newSmd = new File(destinationPath+"\\Testcases\\smd\\"+newName);
				FileWriter fw=new FileWriter(newSmd);
				fw.write(newContent);
				fw.flush();
				fw.close();
				brsmd.close();
				return newSmd;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	private String getNewName(String name) {
		String newName = "";
		if(!name.contains("CONFIG")){
			if(MODE == 1){
				String[] brokenName = name.split("_");
				if(name.contains("ECC")){
					brokenName[3]="0x1";
					brokenName[4]="0x1";				
				}else if(name.contains("SEAP")){
					brokenName[7]="0x1";
					brokenName[8]="0x1";
				}else if(name.contains("SESP")){
					brokenName[2]="0x1";
				}else if(name.contains("ADWL")){
					brokenName[2]="0x1";
					if((name.contains("INF1")||name.contains("I1"))){
						brokenName[3]=RANDOM_ADDRESS_INF2.replaceAll(" 0x", "");
						brokenName[6]="0048.smd";
					}else{
						brokenName[3]=RANDOM_ADDRESS_INF1.replaceAll(" 0x", "");
						brokenName[6]="0049.smd";
					}
				}
				
				for(int i = 0;i<brokenName.length-1;i++)
					newName += brokenName[i]+"_";
				newName += brokenName[brokenName.length-1];
				return newName;
			}else if(MODE == 2){
				String[] brokenName = name.split("_");
				if(name.contains("ECC")){
					brokenName[3]="0x2";
					brokenName[4]="0x1";				
				}else if(name.contains("SEAP")){
					brokenName[7]="0x2";
					brokenName[8]="0x1";
				}else if(name.contains("SESP")){
					brokenName[2]="0x2";
				}
				for(int i = 0;i<brokenName.length-1;i++)
					newName += brokenName[i]+"_";
				newName += brokenName[brokenName.length-1];
				return newName;
			}else if(MODE == 3){
				String[] brokenName = name.split("_");
				if(name.contains("ECC")){
					brokenName[3]="0x2";
					brokenName[4]="0x1";				
				}else if(name.contains("SEAP")){
					brokenName[7]="0x2";
					brokenName[8]="0x1";
				}else if(name.contains("SESP")){
					brokenName[2]="0x2";
				}else if(name.contains("ADWL")){
					brokenName[2]="0x1";
					if((name.contains("INF1")||name.contains("I1"))){
						brokenName[3]=RANDOM_ADDRESS_INF2.replaceAll(" 0x", "");
						brokenName[6]="0048.smd";
					}else{
						brokenName[3]=RANDOM_ADDRESS_INF1.replaceAll(" 0x", "");
						brokenName[6]="0049.smd";
					}
				}
				
				for(int i = 0;i<brokenName.length-1;i++)
					newName += brokenName[i]+"_";
				newName += brokenName[brokenName.length-1];
				return newName;
			}else if(MODE ==4){
				String[] brokenName = name.split("_");
				if(name.contains("ECC")){
					brokenName[3]="0x3";
					brokenName[4]="0x1";				
				}else if(name.contains("SEAP")){
					brokenName[7]="0x3";
					brokenName[8]="0x1";
				}else if(name.contains("SESP")){
					brokenName[2]="0x3";
				}else if(name.contains("ADWL")){
					brokenName[2]="0x1";
					if((name.contains("INF1")||name.contains("I1"))){
						brokenName[3]=RANDOM_ADDRESS_INF2.replaceAll(" 0x", "");
						brokenName[6]="0048.smd";
					}else{
						brokenName[3]=RANDOM_ADDRESS_INF1.replaceAll(" 0x", "");
						brokenName[6]="0049.smd";
					}
				}
				
				for(int i = 0;i<brokenName.length-1;i++)
					newName += brokenName[i]+"_";
				newName += brokenName[brokenName.length-1];
				return newName;
			}
		}
		return name;
	}

	File generateNewSNF(File snf){
		
		String line;
		try {
			BufferedReader brsnf = new BufferedReader(new FileReader(snf));
			ArrayList<String> snflines = new ArrayList<String>();
			while((line=brsnf.readLine())!=null){
				snflines.add(line);
			}
			
			
			
			HashMap<String, HashMap<String, String>> changes = getAllChanges();
			
			System.out.println("Converting required SMDs...");
			for(int i=0;i<snflines.size();i++){
//				System.out.println(i);
				
				line = snflines.get(i);
				if(line == null||line.trim().startsWith("%")||line.trim().equals("")) continue;
				if(line.contains(".snf")){
					File newSnf = generateNewSNF(new File(sourcePath+"\\Testcases\\snf\\"+line));
					snflines.set(i, newSnf.getName());
					continue;
				}
				File newSmd = generateNewSMD(new File(sourcePath+"\\Testcases\\smd\\"+line), changes);
				line = newSmd.getName();
				snflines.set(i, line);
			}
			if(!snf.getName().equals("LE_CONFIG_DEFAULT_PARAM_001.snf"))
					snflines = createAddOnSMD(snflines);
			
			String newContent = "";
			
			for(String s:snflines){
				newContent += s + "\n";
			}
			
			
			
			File newSnf = new File(destinationPath+"\\Testcases\\snf\\"+snf.getName());
			FileWriter fw=new FileWriter(newSnf);
			fw.write(newContent);
			fw.flush();
			fw.close();
			brsnf.close();
			return newSnf;

			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return snf;
	}

	private ArrayList<String> createAddOnSMD(ArrayList<String> snflines) throws IOException {
		System.out.println("Adding required SMDs...");
		if(MODE == 1||MODE == 4){
			try {
				snflines = createSASRAAndSRA(snflines);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(MODE == 2){
			snflines = addADRL_SRPAT_SAREWithoutRandomAddress(snflines);
		}else if(MODE == 3){
			try {
				snflines = addADRL_SRPAT_SAREWithoutRandomAddress(snflines);
				snflines = createSASRAAndSRA(snflines);
				snflines = modifyADRLToRandomAddress(snflines);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			
		}

		return snflines;
	}

	void createAndAddByteDumpsForRA() throws IOException{
		BufferedReader brs = new BufferedReader(new FileReader(new File("resources/C_SASRA_0x00_0x130313031303_INF2_BV_0013.smd")));
		Object[] o = brs.lines().toArray();
		String lines = String.join("\r\n", Arrays.copyOf(o, o.length, String[].class));
		File mlb = new File(destinationPath +"\\Messages\\LE_cmd_msg.mlb");
		
		String byteDump = " 11, 0x01 0x35 0x20 0x07 0x00 "+RANDOM_ADDRESS_INF2;
		
		int var = addByteDump(mlb, "HCI_LE_SET_ADV_SET_RANDOM_ADDR", byteDump);
		lines=lines.replaceAll("<var>", ""+var);
		FileWriter fw = new FileWriter(new File("resources/C_SASRA_0x00_0x130313031303_INF2_BV_0013.smd"));
		fw.write(lines);
		fw.close();
		brs.close();
		brs = new BufferedReader(new FileReader(new File("resources/C_SASRA_0x1_0x111292921211_INF1_BV_0010.smd")));
		o = brs.lines().toArray();
		lines = String.join("\r\n", Arrays.copyOf(o, o.length, String[].class));

		
		byteDump = " 11, 0x01 0x35 0x20 0x07 0x00 "+RANDOM_ADDRESS_INF1;
		
		var = addByteDump(mlb, "HCI_LE_SET_ADV_SET_RANDOM_ADDR", byteDump);
		lines=lines.replaceAll("<var>", ""+var);
		fw = new FileWriter(new File("resources/C_SASRA_0x1_0x111292921211_INF1_BV_0010.smd"));
		fw.write(lines);
		fw.close();
		brs.close();
		brs = new BufferedReader(new FileReader(new File("resources/C_SRA_0x112233332211_INF1_BV_0043.smd")));
		o = brs.lines().toArray();
		lines = String.join("\r\n", Arrays.copyOf(o, o.length, String[].class));

		
		byteDump = "    10, 0x01 0x05 0x20 0x06 "+RANDOM_ADDRESS_INF1;
		
		var = addByteDump(mlb, "LE_SET_RANDOM_ADDRESS", byteDump);
		lines=lines.replaceAll("<var>", ""+var);
		fw = new FileWriter(new File("resources/C_SRA_0x112233332211_INF1_BV_0043.smd"));
		fw.write(lines);
		fw.close();
		brs.close();
		brs = new BufferedReader(new FileReader(new File("resources/C_SRA_0x112233332211_INF2_BV_0042.smd")));
		o = brs.lines().toArray();
		lines = String.join("\r\n", Arrays.copyOf(o, o.length, String[].class));

		
		byteDump = "    10, 0x01 0x05 0x20 0x06 "+RANDOM_ADDRESS_INF2;
		
		var = addByteDump(mlb, "LE_SET_RANDOM_ADDRESS", byteDump);
		lines=lines.replaceAll("<var>", ""+var);
		fw = new FileWriter(new File("resources/C_SRA_0x112233332211_INF2_BV_0042.smd"));
		fw.write(lines);
		fw.close();
		brs.close();
	}
	
	void revertChangesAndCleanUp(){
		File[] list={new File("resources/C_SASRA_0x00_0x130313031303_INF2_BV_0013.smd"),new File("resources/C_SASRA_0x1_0x111292921211_INF1_BV_0010.smd"),
				new File("resources/C_SRA_0x112233332211_INF1_BV_0043.smd"),new File("resources/C_SRA_0x112233332211_INF2_BV_0042.smd")};
		for(int i=0;i<list.length;i++){
			try{
				BufferedReader brs = new BufferedReader(new FileReader(list[i]));
				Object[] o = brs.lines().toArray();
				String lines[] = Arrays.copyOf(o, o.length, String[].class);
				for(int j=0;j<lines.length;j++){
					if(lines[j].contains("HCI_LE_SET_ADV_SET_RANDOM_ADDR")||lines[j].contains("LE_SET_RANDOM_ADDRESS")){
						lines[j].replace(lines[j].split(",")[2].trim(), "<var>");
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		File dir[] = {new File(destinationPath+"\\Testcases\\smd"),new File(destinationPath+"\\Messages")};
		for(int j=0;j<dir.length;j++){
			File[] files = dir[j].listFiles();
			for(int i=0;i<files.length;i++){
				if(files[i].getName().contains("~"))
					files[i].delete();
			}
		}
	}
	
	ArrayList<String> createSASRAAndSRA(ArrayList<String> snflines) throws Exception{
		String infSasra, infSra, tempFileSasra, tempFileSra, randomAddressSasra, randomAddressSra;

		for(int i=0;i<snflines.size();i++){
			if(snflines.get(i) == null||snflines.get(i).trim().startsWith("%")||!snflines.get(i).contains(".smd")||snflines.get(i).contains("CONFIG")) continue;
			if(snflines.get(i).contains("SEAP")){
				if(snflines.get(i).contains("INF1_BV")||snflines.get(i).contains("I1_BV")){
					infSasra = "INF1";
					infSra = "INF2";
					tempFileSasra = "resources/C_SASRA_0x1_0x111292921211_INF1_BV_0010.smd";
					tempFileSra = "resources/C_SRA_0x112233332211_INF2_BV_0042.smd";
					
					randomAddressSasra = RANDOM_ADDRESS_INF1;
					randomAddressSra = RANDOM_ADDRESS_INF2;
				
				
				}else{
					infSasra = "INF2";
					infSra = "INF1";
					tempFileSasra = "resources/C_SASRA_0x00_0x130313031303_INF2_BV_0013.smd";
					tempFileSra = "resources/C_SRA_0x112233332211_INF1_BV_0043.smd";
					randomAddressSasra = RANDOM_ADDRESS_INF2;
					randomAddressSra = RANDOM_ADDRESS_INF1;
				
				}
				File seap = new File(destinationPath +"\\Testcases\\smd\\"+snflines.get(i));
				String ignores = collectIgnores(seap);
				String advHandle="", variant = "", line;
				BufferedReader br = new BufferedReader(new FileReader(seap));
				while((line=br.readLine())!=null){
					if(line.contains("ADV_HANDLE")){
						advHandle = line.split("=")[1].trim();
						break;
					}
					else if(line.contains("SEND")&&line.contains("HCI_LE_SET_EXT_ADV_PARAM"))
						variant = line.split(",")[2].trim();
				}
				br.close();
				if(advHandle.equals("") && variant != null){
					System.out.println("Adv handle not found! Checking in command library...");
					br = new BufferedReader(new FileReader(sourcePath +"\\Messages\\LE_cmd_msg.mlb"));
					while((line=br.readLine())!=null&&!line.trim().startsWith("HCI_LE_SET_EXT_ADV_PARAM"));
					while((line=br.readLine())!=null&&!(line.split(",")[0].trim().startsWith(variant)));
					
					if(line != null){
						System.out.println(line + "------"+variant);
						advHandle = line.split(",")[2].trim().split(" ")[4].trim();
						
						System.out.println("Adv handle found as "+ advHandle + "! Proceeding...");
					}
					br.close();
				}
				else if(advHandle.contains("\"")){
					advHandle = advHandle.substring(1, advHandle.length()-1);
				}
				
				String newNameSasra = "C_SASRA_"+advHandle+"_"+randomAddressSasra.replaceAll(" 0x", "")+"_"+infSasra+"_BV_00";
				String newNameSra = "C_SRA_"+randomAddressSra.replaceAll(" 0x", "")+"_"+infSra+"_BV_00";
				
				File destinationSmdDir = new File(destinationPath+"\\Testcases\\smd\\");
				ArrayList<File> listOfDestinationSmdFiles = new ArrayList<File>(Arrays.asList(destinationSmdDir.listFiles()));
				boolean sasraPresent = false;
				boolean sraPresent = false;
				
				File sasra = new File(tempFileSasra);
				File sra = new File(tempFileSra);
				HashMap<String, HashMap<String, String>> changes = new HashMap<String, HashMap<String, String>>();
				changes.put("HCI_LE_SET_ADV_SET_RANDOM_ADDR", new HashMap<String, String>());					
				changes.get("HCI_LE_SET_ADV_SET_RANDOM_ADDR").put("adv_handle",advHandle);

				
				for(File smd: listOfDestinationSmdFiles){
					if(smd.getName().contains(newNameSasra)&&!(smd.getName().contains("~"))){
						File oldFiletemp = generateNewSMD(sasra, changes);
						insert(oldFiletemp.getAbsolutePath(), 230, ignores.getBytes());
						if(generateCode(smd.getAbsolutePath()).equals(generateCode(oldFiletemp.getAbsolutePath()))){
							sasraPresent = true;
							newNameSasra = smd.getName();
						}
						oldFiletemp.delete();
					}
					if(smd.getName().contains(newNameSra)){
						sraPresent = true;
						newNameSra = smd.getName();
					}
				}
				
				

				if(sasraPresent == false){
					
					newNameSasra += (sasraCounter++)+".smd";
					File oldFileSasra = generateNewSMD(sasra, changes);
					
					insert(oldFileSasra.getAbsolutePath(), 230, ignores.getBytes());
					File newfileSasra = new File(destinationPath+"\\Testcases\\smd\\"+newNameSasra);
					if (oldFileSasra.renameTo(newfileSasra)) {
							System.out.println("success for : "+newNameSasra);
					} else {
						System.out.println("Failure: Rename failed for : " +" -> "+newNameSasra);
						
					}
				}
				
				if(sraPresent == false){
					
					newNameSra += (sraCounter++)+".smd";
					File oldFileSra = generateNewSMD(sra, changes);

					File newfileSra = new File(destinationPath+"\\Testcases\\smd\\"+newNameSra);
					
					if (oldFileSra.renameTo(newfileSra)) {
						System.out.println("success for : "+newNameSra);
					} else {
						System.out.println("Failure: Rename failed for : " +" -> "+newNameSra);
						
					}
				}
				
				
				int j=0;
				while(snflines.get(j).trim().startsWith("%")||snflines.get(j).trim().equals("")
						||snflines.get(j).trim().startsWith("C_RL")||snflines.get(j).contains("CONFIG"))j++;
				
				if(!snflines.get(j).contains(newNameSra)){
					snflines.add(j, newNameSra);
					snflines.add((i+=2), newNameSasra);
				}else{
					snflines.add((i+=1), newNameSasra);
				}

			}

		}
		
		return snflines;
	}

	public String generateCode(String file){
		String line, code="", prevState = "";
		try {
			BufferedReader br=new BufferedReader(new FileReader(new File(file)));
			code = "";
			HashMap<String, Boolean> states=new HashMap<String, Boolean>();		
			while((line=br.readLine())!=null){
				if(line.trim().startsWith("%")||line.trim()==null)
					continue;
				if(line.trim().endsWith(":")){
					prevState = line.trim().split(":")[0];
					states.put(prevState, false);
					continue;	
				}
			}
			br.close();
			br=new BufferedReader(new FileReader(new File(file)));
			while((line=br.readLine())!=null){

				if(line.trim().startsWith("%")||line.trim()==null)
					continue;
				if(line.trim().endsWith(":")){
					//System.out.println("file:	"+file+"	line:	"+line.trim().split(":")[0]);
					
					if(states.get(line.trim().split(":")[0])){
						while((line=br.readLine())!=null&&!(line.trim().endsWith(":")));

					}
					continue;
				}
				if(line.contains("START_TC")||line.contains("END_TC"))
					continue;
				if(line.contains("LOG")){
					br.readLine();
					continue;
				}
					
				if(line.contains("HCI_READ_LOCAL_VERSION_INFO")){
					while((line=br.readLine())!=null&&!(line.trim().endsWith(":")));
				}
				else{

					for(int j=0;j<states.keySet().size();j++){
						if(line.split(" "+(String) states.keySet().toArray()[j]).length>1)
							line=line.split(" "+(String) states.keySet().toArray()[j])[0]
									+line.split(" "+(String) states.keySet().toArray()[j])[1];
						else if(line.split(","+(String) states.keySet().toArray()[j]).length>1)
							line=line.split(","+(String) states.keySet().toArray()[j])[0]
									+","+line.split(","+(String) states.keySet().toArray()[j])[1];
	
	
					}
		line = line.trim().replaceAll(" ", "");
					line = line.trim().replaceAll("\n", "");
					line = line.trim().replaceAll("\t", "");
					code+=line;

					
				}
			}
			br.close();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return code;
	}
	
	String collectIgnores(File smd) throws Exception{
		BufferedReader brsmd = new BufferedReader(new FileReader(smd));
		ArrayList<String> tag = new ArrayList<String>();
		Object[] o = brsmd.lines().toArray();
		String[] lines = Arrays.copyOf(o, o.length, String[].class);

		String content= "";
		for(int i=0;i<lines.length;i++){
			
			if(lines[i].contains("IGNORE,")){
//				System.out.println(lines[i]);
				String ignoreEvent = lines[i].split(",")[1].trim();
				String ignoreVariant = lines[i].split(",")[2].trim();
				
				int j=i;
				while(j>=0&&!(lines[j].contains("CONSTRUCT")&&lines[j].contains(ignoreEvent)&&lines[j].contains(ignoreEvent))){
					if(lines[j].contains("=")){
						tag.add(lines[j].split("=")[1].trim());
					}
					j--;
				}
				if(j!=-1){
					int k=j-1;
					for(String t:tag){
						k=j-1;
						while(k>=0){
							if(lines[k].contains(t)&&(lines[k].indexOf("=")>lines[k].indexOf(t))){
								if(!content.contains(lines[k])){
									content+=lines[k]+"\r\n";									
									break;
								}
							}
							k--;
						}
						
					}
					
					while(j<=i){
						content+=lines[j]+"\r\n";
						j++;
					}
				}else{
					content+=lines[i]+"\r\n";
				}
				
			}
			
			
		}

		return content;
	}
	
	void createADRL_SRPAT_SAREWithoutRandomAddress(){

		File sourceLib[] = {new File(sourcePath + "\\Testcases\\smd\\C_ADRL_0x00_A6A5A4A3A2A1_INF2_BV_0006.smd"),
				new File(sourcePath + "\\Testcases\\smd\\CE_SRPAT_XXXX_INF2_BV_0002.smd"),
				new File(sourcePath + "\\Testcases\\smd\\CE_SARE_0x01_INF2_BV_0008.smd"),
				new File(sourcePath + "\\Testcases\\smd\\C_ADRL_0x00_B6B5B4B3B2B1_INF1_BV_0010.smd"),
				new File(sourcePath + "\\Testcases\\smd\\CE_SRPAT_XXXX_INF1_BV_0001.smd"),
				new File(sourcePath + "\\Testcases\\smd\\CE_SARE_0x01_INF1_BV_0007.smd"),
				new File(sourcePath + "\\Testcases\\smd\\CE_SARE_0x00_INF2_BV_0002.smd"),
				new File(sourcePath + "\\Testcases\\smd\\CE_SARE_0x00_INF1_BV_0006.smd")};
		File destinationLib[] = {new File(destinationPath + "\\Testcases\\smd\\C_ADRL_0x00_A6A5A4A3A2A1_INF2_BV_0006.smd"),
				new File(destinationPath + "\\Testcases\\smd\\CE_SRPAT_XXXX_INF2_BV_0002.smd"),
				new File(destinationPath + "\\Testcases\\smd\\CE_SARE_0x01_INF2_BV_0008.smd"),
				new File(destinationPath + "\\Testcases\\smd\\C_ADRL_0x00_B6B5B4B3B2B1_INF1_BV_0010.smd"),
				new File(destinationPath + "\\Testcases\\smd\\CE_SRPAT_XXXX_INF1_BV_0001.smd"),
				new File(destinationPath + "\\Testcases\\smd\\CE_SARE_0x01_INF1_BV_0007.smd"),
				new File(destinationPath + "\\Testcases\\smd\\CE_SARE_0x00_INF2_BV_0002.smd"),
				new File(destinationPath + "\\Testcases\\smd\\CE_SARE_0x00_INF1_BV_0006.smd")};

		for(int i=0;i<sourceLib.length;i++){
			System.out.println("Copying "+ sourceLib[i].getName() + " from " + " " + sourcePath + " ...");
			try {
				Files.copy(sourceLib[i].toPath(),destinationLib[i].toPath(),StandardCopyOption.REPLACE_EXISTING);
				System.out.println(sourceLib[i].getName()+" copied successfully!");

			} catch (IOException e) {

				e.printStackTrace();
				System.out.println("Failure: "+sourceLib[i].getName()+" could not be copied");
			}
			
		}

	}
	
	ArrayList<String> addADRL_SRPAT_SAREWithoutRandomAddress(ArrayList<String> snflines){
		int j=0;
		while(snflines.get(j).trim().startsWith("%")||snflines.get(j).trim().equals("")
				||snflines.get(j).trim().startsWith("C_RL")||snflines.get(j).contains("CONFIG"))j++;
		
		snflines.add(j++, "C_ADRL_0x00_A6A5A4A3A2A1_INF2_BV_0006.smd");
		snflines.add(j++,"CE_SRPAT_XXXX_INF2_BV_0002.smd");
		snflines.add(j++, "CE_SARE_0x01_INF2_BV_0008.smd");
		
		snflines.add(j++, "C_ADRL_0x00_B6B5B4B3B2B1_INF1_BV_0010.smd");
		snflines.add(j++,"CE_SRPAT_XXXX_INF1_BV_0001.smd");
		snflines.add(j++, "CE_SARE_0x01_INF1_BV_0007.smd");
		
		snflines.add("CE_SARE_0x00_INF2_BV_0002.smd");
		snflines.add("CE_SARE_0x00_INF1_BV_0006.smd");

		return snflines;
	}
	
	void addRandomAddressToADRL_SRPAT_SARE(){

		File destinationLib[] = {new File(destinationPath + "\\Testcases\\smd\\C_ADRL_0x00_A6A5A4A3A2A1_INF2_BV_0006.smd"),
				new File(destinationPath + "\\Testcases\\smd\\C_ADRL_0x00_B6B5B4B3B2B1_INF1_BV_0010.smd")};
		File renameLib[] = {new File(destinationPath + "\\Testcases\\smd\\C_ADRL_0x01_"+RANDOM_ADDRESS_INF1.replaceAll(" 0x", "")+"_INF2_BV_0006.smd"),
				new File(destinationPath + "\\Testcases\\smd\\C_ADRL_0x01_"+RANDOM_ADDRESS_INF2.replaceAll(" 0x", "")+"_INF1_BV_0010.smd")};


		for(int i=0;i<destinationLib.length;i++){
			try {
				BufferedReader brl = new BufferedReader(new FileReader(destinationLib[i]));
				Object[] o = brl.lines().toArray();
				String lines = String.join("\r\n", Arrays.copyOf(o, o.length, String[].class));
				File mlb = new File(destinationPath +"\\Messages\\LE_cmd_msg.mlb");
				String randomAddress = (destinationLib[i].getName().contains("INF1"))?RANDOM_ADDRESS_INF2:RANDOM_ADDRESS_INF1;
				String variant = (destinationLib[i].getName().contains("INF1"))?" 1,":" 0,";
				String byteDump = " 43, 0x01 0x27 0x20 0x27 0x01 " + randomAddress + " 0x33 0x33 0x33 0x33 0x33 0x33 0x33 0x33"+
																					" 0x33 0x33 0x33 0x33 0x33 0x33 0x33 0x33"+
																					" 0x44 0x44 0x44 0x44 0x44 0x44 0x44 0x44"+
																					" 0x44 0x44 0x44 0x44 0x44 0x44 0x44 0x44";
				
				int var = addByteDump(mlb, "HCI_LE_ADD_DEV_TO_RESOLVING_LIST", byteDump);
				lines=lines.replaceAll(variant, ""+var+",");
				FileWriter fw = new FileWriter(destinationLib[i]);
				fw.write(lines);
				fw.close();
				brl.close();
				destinationLib[i].renameTo(renameLib[i]);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

	}
	
	ArrayList<String> modifyADRLToRandomAddress(ArrayList<String> snflines){
		for(int j=0;j<snflines.size();j++){
			if(snflines.get(j).equals("C_ADRL_0x00_A6A5A4A3A2A1_INF2_BV_0006.smd"))
				snflines.set(j, "C_ADRL_0x01_"+RANDOM_ADDRESS_INF1.replaceAll(" 0x", "")+"_INF2_BV_0006.smd");
			else if(snflines.get(j).equals("C_ADRL_0x00_B6B5B4B3B2B1_INF1_BV_0010.smd"))
				snflines.set(j, "C_ADRL_0x01_"+RANDOM_ADDRESS_INF2.replaceAll(" 0x", "")+"_INF1_BV_0010.smd");
		}
		return snflines;
	}
	
	public HashMap<String, HashMap<String, String>> getAllChanges() {
		
		HashMap<String, HashMap<String, String>> changes = new HashMap<String, HashMap<String, String>>();
		
		if(MODE == 1){
			
			changes.put("HCI_LE_SET_EXT_ADV_PARAM", new HashMap<String, String>());					
			changes.get("HCI_LE_SET_EXT_ADV_PARAM").put("own_addr_type","0x01");
			changes.get("HCI_LE_SET_EXT_ADV_PARAM").put("peer_addr_type","0x01");
			changes.get("HCI_LE_SET_EXT_ADV_PARAM").put("peer_addr ","P_ADDRESS");
			
			changes.put("HCI_LE_EXTD_CREATE_CONN", new HashMap<String, String>());					
			changes.get("HCI_LE_EXTD_CREATE_CONN").put("own_address_type","0x01");
			changes.get("HCI_LE_EXTD_CREATE_CONN").put("peer_address_type","0x01");
			changes.get("HCI_LE_EXTD_CREATE_CONN").put("peer_address ","P_ADDRESS");
			
			changes.put("HCI_LE_ENHANCED_CONN_COMPLETE_EVENT", new HashMap<String, String>());
			changes.get("HCI_LE_ENHANCED_CONN_COMPLETE_EVENT").put("peer_addr_type","0x01");
			changes.get("HCI_LE_ENHANCED_CONN_COMPLETE_EVENT").put("peer_addr ","P_ADDRESS");
			
			changes.put("HCI_LE_SET_EXT_SCAN_PARAM", new HashMap<String, String>());
			changes.get("HCI_LE_SET_EXT_SCAN_PARAM").put("own_addr_type","0x01");
			
			changes.put("HCI_LE_SET_EXT_SCAN_PARAM", new HashMap<String, String>());
			changes.get("HCI_LE_SET_EXT_SCAN_PARAM").put("own_addr_type","0x01");
			
			changes.put("HCI_LE_PER_ADV_CREATE_SYNC", new HashMap<String, String>());
			changes.get("HCI_LE_PER_ADV_CREATE_SYNC").put("addr_type","0x01");
			changes.get("HCI_LE_PER_ADV_CREATE_SYNC").put("address","P_ADDRESS");
			
			changes.put("HCI_LE_ADD_DEV_TO_WHITE_LIST", new HashMap<String, String>());
			changes.get("HCI_LE_ADD_DEV_TO_WHITE_LIST").put("bd_addr_type","0x01");
			changes.get("HCI_LE_ADD_DEV_TO_WHITE_LIST").put("bd_addr ","P_ADDRESS");
			
			changes.put("LE_PER_ADV_SYNC_ESTABLISHED_EVENT", new HashMap<String, String>());
			changes.get("LE_PER_ADV_SYNC_ESTABLISHED_EVENT").put("adv_addr_type","0x01");
			changes.get("LE_PER_ADV_SYNC_ESTABLISHED_EVENT").put("adv_address ","P_ADDRESS");
			
			changes.put("LE_EXTENDED_ADVERTISING_REPORT", new HashMap<String, String>());
			changes.get("LE_EXTENDED_ADVERTISING_REPORT").put("addr_type","0x01");
			changes.get("LE_EXTENDED_ADVERTISING_REPORT").put("address ","P_ADDRESS");
			
		}else if(MODE == 2){
			
			changes.put("HCI_LE_SET_EXT_ADV_PARAM", new HashMap<String, String>());					
			changes.get("HCI_LE_SET_EXT_ADV_PARAM").put("own_addr_type","0x02");
			
			changes.put("HCI_LE_EXTD_CREATE_CONN", new HashMap<String, String>());					
			changes.get("HCI_LE_EXTD_CREATE_CONN").put("own_address_type","0x02");
			
			changes.put("HCI_LE_ENHANCED_CONN_COMPLETE_EVENT", new HashMap<String, String>());
			changes.get("HCI_LE_ENHANCED_CONN_COMPLETE_EVENT").put("peer_addr_type","0x02");
			
			changes.put("HCI_LE_SET_EXT_SCAN_PARAM", new HashMap<String, String>());
			changes.get("HCI_LE_SET_EXT_SCAN_PARAM").put("own_addr_type","0x02");
			
			changes.put("LE_PER_ADV_SYNC_ESTABLISHED_EVENT", new HashMap<String, String>());
			changes.get("LE_PER_ADV_SYNC_ESTABLISHED_EVENT").put("adv_addr_type","0x02");
			
			changes.put("LE_EXTENDED_ADVERTISING_REPORT", new HashMap<String, String>());
			changes.get("LE_EXTENDED_ADVERTISING_REPORT").put("addr_type","0x02");
			
		}else if(MODE == 3){
			
			changes.put("HCI_LE_SET_EXT_ADV_PARAM", new HashMap<String, String>());					
			changes.get("HCI_LE_SET_EXT_ADV_PARAM").put("own_addr_type","0x02");
			changes.get("HCI_LE_SET_EXT_ADV_PARAM").put("peer_addr_type","0x01");
			changes.get("HCI_LE_SET_EXT_ADV_PARAM").put("peer_addr ","P_ADDRESS");
			
			changes.put("HCI_LE_EXTD_CREATE_CONN", new HashMap<String, String>());					
			changes.get("HCI_LE_EXTD_CREATE_CONN").put("own_address_type","0x02");
			changes.get("HCI_LE_EXTD_CREATE_CONN").put("peer_address_type","0x01");
			changes.get("HCI_LE_EXTD_CREATE_CONN").put("peer_address ","P_ADDRESS");
			
			changes.put("HCI_LE_ENHANCED_CONN_COMPLETE_EVENT", new HashMap<String, String>());
			changes.get("HCI_LE_ENHANCED_CONN_COMPLETE_EVENT").put("peer_addr_type","0x02");
			changes.get("HCI_LE_ENHANCED_CONN_COMPLETE_EVENT").put("peer_addr ","P_ADDRESS");
			
			changes.put("HCI_LE_SET_EXT_SCAN_PARAM", new HashMap<String, String>());
			changes.get("HCI_LE_SET_EXT_SCAN_PARAM").put("own_addr_type","0x02");
			
			changes.put("HCI_LE_PER_ADV_CREATE_SYNC", new HashMap<String, String>());
			changes.get("HCI_LE_PER_ADV_CREATE_SYNC").put("addr_type","0x01");
			changes.get("HCI_LE_PER_ADV_CREATE_SYNC").put("address","P_ADDRESS");
			
			changes.put("HCI_LE_ADD_DEV_TO_WHITE_LIST", new HashMap<String, String>());
			changes.get("HCI_LE_ADD_DEV_TO_WHITE_LIST").put("bd_addr_type","0x01");
			changes.get("HCI_LE_ADD_DEV_TO_WHITE_LIST").put("bd_addr ","P_ADDRESS");
			
			changes.put("LE_PER_ADV_SYNC_ESTABLISHED_EVENT", new HashMap<String, String>());
			changes.get("LE_PER_ADV_SYNC_ESTABLISHED_EVENT").put("adv_addr_type","0x02");
			changes.get("LE_PER_ADV_SYNC_ESTABLISHED_EVENT").put("adv_address ","P_ADDRESS");
			
			changes.put("LE_EXTENDED_ADVERTISING_REPORT", new HashMap<String, String>());
			changes.get("LE_EXTENDED_ADVERTISING_REPORT").put("addr_type","0x02");
			changes.get("LE_EXTENDED_ADVERTISING_REPORT").put("address ","P_ADDRESS");
			
		}else{
			changes.put("HCI_LE_SET_EXT_ADV_PARAM", new HashMap<String, String>());					
			changes.get("HCI_LE_SET_EXT_ADV_PARAM").put("own_addr_type","0x03");
			changes.get("HCI_LE_SET_EXT_ADV_PARAM").put("peer_addr_type","0x01");
			changes.get("HCI_LE_SET_EXT_ADV_PARAM").put("peer_addr ","P_ADDRESS");
			
			changes.put("HCI_LE_EXTD_CREATE_CONN", new HashMap<String, String>());					
			changes.get("HCI_LE_EXTD_CREATE_CONN").put("own_address_type","0x03");
			changes.get("HCI_LE_EXTD_CREATE_CONN").put("peer_address_type","0x01");
			changes.get("HCI_LE_EXTD_CREATE_CONN").put("peer_address ","P_ADDRESS");
			
			changes.put("HCI_LE_ENHANCED_CONN_COMPLETE_EVENT", new HashMap<String, String>());
			changes.get("HCI_LE_ENHANCED_CONN_COMPLETE_EVENT").put("peer_addr_type","0x03");
			changes.get("HCI_LE_ENHANCED_CONN_COMPLETE_EVENT").put("peer_addr ","P_ADDRESS");
			
			changes.put("HCI_LE_SET_EXT_SCAN_PARAM", new HashMap<String, String>());
			changes.get("HCI_LE_SET_EXT_SCAN_PARAM").put("own_addr_type","0x03");
			
			changes.put("HCI_LE_PER_ADV_CREATE_SYNC", new HashMap<String, String>());
			changes.get("HCI_LE_PER_ADV_CREATE_SYNC").put("addr_type","0x01");
			changes.get("HCI_LE_PER_ADV_CREATE_SYNC").put("address","P_ADDRESS");
			
			changes.put("HCI_LE_ADD_DEV_TO_WHITE_LIST", new HashMap<String, String>());
			changes.get("HCI_LE_ADD_DEV_TO_WHITE_LIST").put("bd_addr_type","0x01");
			changes.get("HCI_LE_ADD_DEV_TO_WHITE_LIST").put("bd_addr ","P_ADDRESS");
			
			changes.put("LE_PER_ADV_SYNC_ESTABLISHED_EVENT", new HashMap<String, String>());
			changes.get("LE_PER_ADV_SYNC_ESTABLISHED_EVENT").put("adv_addr_type","0x03");
			changes.get("LE_PER_ADV_SYNC_ESTABLISHED_EVENT").put("adv_address ","P_ADDRESS");
			
			changes.put("LE_EXTENDED_ADVERTISING_REPORT", new HashMap<String, String>());
			changes.get("LE_EXTENDED_ADVERTISING_REPORT").put("addr_type","0x03");
			changes.get("LE_EXTENDED_ADVERTISING_REPORT").put("address ","P_ADDRESS");
			
		}
		
		return changes;
	}
	
	ArrayList<String> getAndFilterListOfPublicScenarios(String csvPath){
		
		ArrayList<String> finalList = new ArrayList<String>();
		String line;
		File csv = new File(csvPath);
		
		try {
			System.out.println("Reading list of scenarios...");
			BufferedReader brcsv = new BufferedReader(new FileReader(csv));
			while((line=brcsv.readLine())!=null){
				finalList.add(line.trim());
			}
			brcsv.close();
			System.out.println("Filtering the list of scenarios read...");
			for(int i=0;i<finalList.size();i++){
				
				if(checkScenario(finalList.get(i)).equals("false")){
					System.out.println(finalList.get(i) + " is not public and has been removed!");
					finalList.remove(i);
					i--;
					System.out.println("Filtering the list of scenarios read...");
				}
				
			}
			System.out.println("List of scenarios read and filtered successfully!");
			return finalList;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		return null;
	}
	
	private String checkScenario(String snf) {
		String line, result = "true";
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(sourcePath + "\\Testcases\\snf\\" + snf)));

			while((line = br.readLine())!=null){
				if(line.trim().equals("")||line.trim().startsWith("%"))
					continue;
				if(line.contains("C_ADRL"))
					result = "false";
				else if(line.contains("C_SRA"))
					result = "false";
				else if(line.contains("C_SASRA"))
					result = "false";
				else if(line.contains(".snf"))
					result = checkScenario(line);
			}
			
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			return "false";
		} catch (IOException e) {
			
			e.printStackTrace();
			return "false";
		}
		
		return result;
	}

	void copyMLB(){

		File sourceLib[] = {new File(sourcePath + "\\Messages\\LE_cmd_msg.mlb"),new File(sourcePath + "\\Messages\\LE_evt_msg.mlb")};
		File destinationLib[] = {new File(destinationPath + "\\Messages\\LE_cmd_msg.mlb"),new File(destinationPath + "\\Messages\\LE_evt_msg.mlb")};



		for(int i=0;i<sourceLib.length;i++){
			try {
				Files.copy(sourceLib[i].toPath(),destinationLib[i].toPath(),StandardCopyOption.REPLACE_EXISTING);
				System.out.println(sourceLib[i].getName()+" copied successfully!");
			} catch (IOException e) {

				e.printStackTrace();
			}

			
		}
		
		
	}
	
	void createWorkspace(String referencePath){
		
		System.out.println("Creating workspace...");
        String suiteName;
		
		if(MODE == 1){
			suiteName = "AE_RA";
		}else if(MODE == 2){
			suiteName = "AE_RPA";
		}else if(MODE == 3){
			suiteName = "AE_RPA_with_RA";
		}else{
			suiteName = "AE_nRPA";
		}
		File snfPath = new File(referencePath + "\\" + suiteName + "\\Master_Testsuite\\MUTE_workspace\\Testcases\\snf");
		File smdPath = new File(referencePath + "\\" + suiteName + "\\Master_Testsuite\\MUTE_workspace\\Testcases\\smd");
		File ssnPath = new File(referencePath + "\\" + suiteName + "\\Master_Testsuite\\MUTE_workspace\\Testcases\\ssn");
		File mlbPath = new File(referencePath + "\\" + suiteName + "\\Master_Testsuite\\MUTE_workspace\\Messages");
		
		if(snfPath.mkdirs()&&
				smdPath.mkdirs()&&
				ssnPath.mkdirs()&&
				mlbPath.mkdirs())
			System.out.println("Workspace created successfully!");
		else{
			System.out.println("Failure: Workspace could not be created!");
			
		}
		destinationPath = referencePath + "\\" + suiteName + "\\Master_Testsuite\\MUTE_workspace";
		copyMLB();
		
		
		
	}
	
	void generateTestSuite(String pathToListFile){
		System.out.println("Test suite generation started...");
		ArrayList<String> listOfPublicScenarios = getAndFilterListOfPublicScenarios(pathToListFile);
		listOfPublicScenarios.add("LE_INIT_5.0_MT_INF1_MT_INF2.snf");
		listOfPublicScenarios.add("LE_CONFIG_DEFAULT_PARAM_001.snf");
		String contentForSSN = "LE_CONFIG_DEFAULT_PARAM_001.snf\n";
		String ssnName = "AE_PRIV";
		
		if(MODE == 1){
			ssnName = "AE_RA";			
		}else if(MODE == 2){
			ssnName = "AE_RPA";
		}else if(MODE == 3){
			ssnName = "AE_RPA_with_RA";
		}else{
			ssnName = "AE_nRPA";
		}
		File ssn = new File(destinationPath + "\\Testcases\\ssn\\" + ssnName +".ssn");
		
		
		try {
			ssn.createNewFile();
			if(MODE == 1||MODE == 4) createAndAddByteDumpsForRA();
			else if(MODE == 2)createADRL_SRPAT_SAREWithoutRandomAddress();
			else if(MODE == 3){
				createAndAddByteDumpsForRA();
				createADRL_SRPAT_SAREWithoutRandomAddress();
				addRandomAddressToADRL_SRPAT_SARE();
			}else{
				
			}
			for(String scenario : listOfPublicScenarios){
				System.out.println("Generating scenario "+scenario+" ...");
				File oldScenario = new File(sourcePath+"\\Testcases\\snf\\"+scenario);
				File newScenario = generateNewSNF(oldScenario);
				
				if(oldScenario.getAbsolutePath().equals(newScenario.getAbsolutePath())){
					System.out.println(oldScenario.getName() + "failed to be generated!");
				}else if(!(scenario.equals("LE_INIT_5.0_MT_INF1_MT_INF2.snf")||scenario.equals("LE_CONFIG_DEFAULT_PARAM_001.snf"))){
					System.out.println("Scenario "+newScenario.getName()+" generated successfully!");
					contentForSSN += "LE_INIT_5.0_MT_INF1_MT_INF2.snf\n"+scenario+"\n";
				}

			}
			System.out.println("Creating session file...");
			FileWriter fw=new FileWriter(ssn);
			fw.write(contentForSSN);
			fw.flush();
			fw.close();
			System.out.println("Session file created successfully!");
			System.out.println("-----Test suite generated successfully!");
		} catch (IOException e) {
			
			e.printStackTrace();
			System.out.println("Failure: Test suite generation failed!");
		}
		
		
	}

	int addByteDump(File mlb, String command, String byteDump){
		long offset = 0 ;
		
		BufferedReader brmlb;
		try {
			brmlb = new BufferedReader(new FileReader(mlb));
			Object[] o = brmlb.lines().toArray();
			String lines = String.join("\r\n", Arrays.copyOf(o, o.length, String[].class));

			int lastcomma =lines.lastIndexOf(",", lines.lastIndexOf(",",lines.indexOf(":\r\n",lines.indexOf(":",lines.indexOf(command+":"))+1))-1);
			int lastline = lines.lastIndexOf("\n",lastcomma-1);
			int var = Integer.parseInt(lines.substring(lastline, lastcomma).trim())+1;
			offset = lines.indexOf("\n", lines.indexOf("\n", lines.lastIndexOf(",",lines.indexOf(":\r\n",lines.indexOf(":",lines.indexOf(command+":"))+1)))) + 1;
			

			insert(mlb.getAbsolutePath(),offset,("\r\n    "+var+","+byteDump+"\r\n").getBytes());
			return var;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return -1;
	}
	
	public static void insert(String filename, long offset, byte[] content) throws Exception {
		  RandomAccessFile r = new RandomAccessFile(new File(filename), "rw");
		  RandomAccessFile rtemp = new RandomAccessFile(new File(filename + "~"), "rw");
		  long fileSize = r.length();
		  FileChannel sourceChannel = r.getChannel();
		  FileChannel targetChannel = rtemp.getChannel();
		  sourceChannel.transferTo(offset, (fileSize - offset), targetChannel);
		  sourceChannel.truncate(offset);
		  r.seek(offset);
		  r.write(content);
		  long newOffset = r.getFilePointer();
		  targetChannel.position(0L);
		  sourceChannel.transferFrom(targetChannel, newOffset, (fileSize - offset));
		  sourceChannel.close();
		  targetChannel.close();
		}
	
	public static void main(String[] args) {
		

		RAReplicator r1 = new RAReplicator(4);

		r1.setRANDOM_ADDRESS_INF1("0x11 0x22 0x33 0x33 0x22 0x11");
		r1.setRANDOM_ADDRESS_INF2("0xAA 0xBB 0xCC 0xCC 0xBB 0xAA");
		r1.setSourcePath("D:\\FreshCheckout\\MUTE_workspace");
		r1.setSasraCounter(17);
		r1.setSraCounter(42);
		r1.createWorkspace("D:\\Ayaz\\run");
		r1.generateTestSuite("D:\\Ayaz\\listOfScenarios.csv");
        r1.revertChangesAndCleanUp();
	}

}
