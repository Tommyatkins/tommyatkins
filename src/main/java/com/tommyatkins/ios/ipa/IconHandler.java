package com.tommyatkins.ios.ipa;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

import com.jcraft.jzlib.ZStream;

public class IconHandler {

	private static PNGTrunk getTrunk(List<PNGTrunk> trunks, String szName) {
		if (trunks == null) {
			return null;
		}

		for (int n = 0; n < trunks.size(); n++) {
			PNGTrunk trunk = (PNGTrunk) trunks.get(n);
			if (trunk.getName().equalsIgnoreCase(szName)) {
				return trunk;
			}
		}
		return null;
	}

	public static boolean convertPNGFile(byte[] iconBytes, File pngFile) {
		ArrayList<PNGTrunk> trunks = null;
		FileOutputStream output = null;
		DataInputStream in = null;
		ByteArrayInputStream bis = null;
		try {
			bis = new ByteArrayInputStream(iconBytes);
			in = new DataInputStream(bis);
			byte[] nPNGHeader = new byte[8];
			in.read(nPNGHeader);

			trunks = new ArrayList<PNGTrunk>();

			if ((nPNGHeader[0] == -119) && (nPNGHeader[1] == 80) && (nPNGHeader[2] == 78) && (nPNGHeader[3] == 71) && (nPNGHeader[4] == 13)
					&& (nPNGHeader[5] == 10) && (nPNGHeader[6] == 26) && (nPNGHeader[7] == 10)) {
				PNGTrunk trunk;
				do {
					trunk = PNGTrunk.generateTrunk(in);
					trunks.add(trunk);
					if (trunk!=null && trunk.getName().equalsIgnoreCase("CgBI")) {
						/**
						 * bWithCgBI
						 */
					}
				} while (trunk!=null && !trunk.getName().equalsIgnoreCase("IEND"));
			}

			if (getTrunk(trunks, "CgBI") != null) {

				PNGTrunk dataTrunk = getTrunk(trunks, "IDAT");

				PNGIHDRTrunk ihdrTrunk = (PNGIHDRTrunk) getTrunk(trunks, "IHDR");
				// System.out.println("Width:" + ihdrTrunk.m_nWidth + " Height:"
				// + ihdrTrunk.m_nHeight);

				int nMaxInflateBuffer = 4 * (ihdrTrunk.m_nWidth + 1) * ihdrTrunk.m_nHeight;
				byte[] outputBuffer = new byte[nMaxInflateBuffer];

				ZStream inStream = new ZStream();
				inStream.avail_in = dataTrunk.getSize();
				inStream.next_in_index = 0;
				inStream.next_in = dataTrunk.getData();
				inStream.next_out_index = 0;
				inStream.next_out = outputBuffer;
				inStream.avail_out = outputBuffer.length;

				if (inStream.inflateInit(-15) != 0) {
					System.out.println("PNGCONV_ERR_ZLIB");
					return false;
				}

				int nResult = inStream.inflate(0);
				switch (nResult) {
				case 2:
					nResult = -3;
				case -4:
				case -3:
					inStream.inflateEnd();
					System.out.println("PNGCONV_ERR_ZLIB");
					return false;
				}

				nResult = inStream.inflateEnd();

				if (inStream.total_out > nMaxInflateBuffer) {
					System.out.println("PNGCONV_ERR_INFLATED_OVER");
				}

				int nIndex = 0;

				for (int y = 0; y < ihdrTrunk.m_nHeight; y++) {
					nIndex++;
					for (int x = 0; x < ihdrTrunk.m_nWidth; x++) {
						byte nTemp = outputBuffer[nIndex];
						outputBuffer[nIndex] = outputBuffer[(nIndex + 2)];
						outputBuffer[(nIndex + 2)] = nTemp;
						nIndex += 4;
					}
				}

				ZStream deStream = new ZStream();
				int nMaxDeflateBuffer = nMaxInflateBuffer + 1024;
				byte[] deBuffer = new byte[nMaxDeflateBuffer];

				deStream.avail_in = outputBuffer.length;
				deStream.next_in_index = 0;
				deStream.next_in = outputBuffer;
				deStream.next_out_index = 0;
				deStream.next_out = deBuffer;
				deStream.avail_out = deBuffer.length;
				deStream.deflateInit(9);
				nResult = deStream.deflate(4);

				if (deStream.total_out > nMaxDeflateBuffer) {
					System.out.println("PNGCONV_ERR_DEFLATED_OVER");
				}
				byte[] newDeBuffer = new byte[(int) deStream.total_out];
				for (int n = 0; n < deStream.total_out; n++) {
					newDeBuffer[n] = deBuffer[n];
				}
				CRC32 crc32 = new CRC32();
				crc32.update(dataTrunk.getName().getBytes());
				crc32.update(newDeBuffer);
				long lCRCValue = crc32.getValue();

				dataTrunk.m_nData = newDeBuffer;
				dataTrunk.m_nCRC[0] = (byte) (int) ((lCRCValue & 0xFF000000) >> 24);
				dataTrunk.m_nCRC[1] = (byte) (int) ((lCRCValue & 0xFF0000) >> 16);
				dataTrunk.m_nCRC[2] = (byte) (int) ((lCRCValue & 0xFF00) >> 8);
				dataTrunk.m_nCRC[3] = (byte) (int) (lCRCValue & 0xFF);
				dataTrunk.m_nSize = newDeBuffer.length;

				output = new FileOutputStream(pngFile);
				byte[] pngHeader = { -119, 80, 78, 71, 13, 10, 26, 10 };
				output.write(pngHeader);
				for (int n = 0; n < trunks.size(); n++) {
					PNGTrunk trunk = (PNGTrunk) trunks.get(n);
					if (trunk!=null) {
						if (trunk.getName().equalsIgnoreCase("CgBI")) {
							continue;
						}
						trunk.writeToStream(output);
					}
				}
				return true;
			}
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (output != null) {
					output.close();
				}
				if (in != null) {
					in.close();
				}
				if (bis != null) {
					bis.close();
				}
			} catch (IOException e) {
				// TODO: handle exception
			}
		}
		return false;

	}
}