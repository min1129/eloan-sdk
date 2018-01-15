package com.softkey;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.rongzer.utils.ByteUtils;

public class jsyunew4
{

	//测试helloworld
	public static native String helloWorld();
	
	//测试a+b
	public static native int add(int a,int b);
	
	//查找加密锁
	public static native String FindPort(int start);
	
	//获到锁的版本
	public static native int GetVersion(String InPath);
	
	//设置被注册的SM2证书
	private static native int WriteData(byte[] inBuf, int nStart, String InPath);
	
	//读取被设置的SMS证书
	private static native byte[] ReadData(int nStart,int nLen , String InPath);
	
	//获到锁的版本
	public static native int SetPin(String oldPin , String newPin , String InPath);

	//由锁创建公私钥并返回公钥与用户信息
	public static native byte[] GenKeyPair(String sm2UserName , String InPath);
	
	//设置密钥对
	public static native int SetKeyPair(byte[] priKey,byte[] pubKeyX,byte[] pubKeyY,String sm2UserName,String InPath);
	
	//返回公钥与用户名
	public static native byte[] GetPubKey(String InPath);
		
	//对消息进行签名
	public static native byte[] SM2Sign(byte[] inBuf,String Pin,String InPath);
	
	//对加密消息进行解密
	public static native byte[] SM2Dec(byte[] inBuf,String Pin,String InPath);
	
	//向读写存储器写入证书
	public static int WriteSM2Cert(byte[] certBuf,String inPath)
	{
		return WriteDataEx(certBuf,0,inPath);
	}
	
	//从读写存储器读入证书
	public static byte[] ReadSM2Cert(String inPath)
	{
		return ReadDataEx(0,490,inPath);
	}
	
	/**
	 * 向读写存储器中写入字节流，读写存储器最多494个字节
	 * @param inBuf
	 * @param nStart
	 * @param InPath
	 * @return
	 */
	public static int WriteDataEx(byte[] inBuf, int nStart, String InPath)
	{
		//增加结束符
		inBuf = ByteUtils.addEnd(inBuf);
		int nRet = 0;
		int nLen = inBuf.length;
		for (int i=0;i<nLen/MAX_BUF;i++)
		{
			byte[] bData = ByteUtils.subBuf(inBuf,i*MAX_BUF,MAX_BUF);
			nRet = WriteData(bData,i*MAX_BUF+nStart,InPath);
			if (nRet != 0)
			{
				return nRet;
			}
		}
		
		if (nLen%MAX_BUF != 0)
		{
			byte[] bData = ByteUtils.subBuf(inBuf,(nLen/MAX_BUF)*MAX_BUF,nLen%MAX_BUF);
			nRet = WriteData(bData,(nLen/MAX_BUF)*MAX_BUF+nStart,InPath);
			if (nRet != 0)
			{
				return nRet;
			}
		}
		return nRet;
	}
	
	/**
	 * 从读写存储器中读取字节流，读写存储器最多494个字节
	 * @param nStart
	 * @param nLen
	 * @param InPath
	 * @return
	 */
	public static byte[] ReadDataEx(int nStart,int nLen , String InPath)
	{
		byte[] bReturn = new byte[nLen];

		for (int i=0;i<nLen/MAX_BUF;i++)
		{
			byte[] bData = ReadData(i*MAX_BUF+nStart,MAX_BUF,InPath);
			for (int j=0;j<bData.length;j++)
			{
				bReturn[i*MAX_BUF+j] = bData[j];
			}
		}
		
		if (nLen%MAX_BUF != 0)
		{
			byte[] bData = ReadData((nLen/MAX_BUF)*MAX_BUF+nStart,nLen%MAX_BUF,InPath);
			for (int j=0;j<bData.length;j++)
			{
				bReturn[(nLen/MAX_BUF)*MAX_BUF+j] = bData[j];
			}
		}
		
		return ByteUtils.trimBuf(bReturn);
		
	}
	
	static int  MAX_BUF = 128;

	static
     {

		//判断操作系统
		try
		{
			String osName = System.getProperty("os.name");
			String archModel =  System.getProperty("sun.arch.data.model");
			
			String libName = "/libso/jsyunew4-32.dll";
			if (osName.toLowerCase().indexOf("windows") >=0)
			{
				if ("64".equals(archModel))
				{
					libName = "/libso/jsyunew4-64.dll";
				}else
				{
					libName = "/libso/jsyunew4-32.dll";

				}
				Resource resource = new ClassPathResource(libName);
				if (resource.exists())
				{
					System.load(resource.getFile().getAbsolutePath());
				}
				System.out.println(libName);

				
			}else
			{
				//在linux上读写buf的性能较低
				MAX_BUF = 8;
				libName = "libjsyunew4-linux.so";
				System.out.println("java.library.path:"+System.getProperty("java.library.path"));
				System.loadLibrary("jsyunew4-linux");

			}

		}catch(Exception e)
		{
			e.printStackTrace();
		}				

		
        // System.loadLibrary("Jsyunew3");
     }
 
}