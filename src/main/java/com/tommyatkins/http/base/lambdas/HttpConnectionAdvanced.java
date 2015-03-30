package com.tommyatkins.http.base.lambdas;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tommyatkins.http.base.HttpConnection;
import com.tommyatkins.http.base.HttpConnection.RequestMethod;
import com.tommyatkins.http.base.lambdas.process.DefaultProcess;
import com.tommyatkins.http.base.lambdas.process.enu.ProcessPoint;
import com.tommyatkins.http.base.lambdas.process.functional.HttpConnectionProcess;

/**
 * 
 * @author TOMMYATKINS
 *
 */
public class HttpConnectionAdvanced {

	public static void main(String[] args) throws IOException {
		//&to=b60ae64ea919c9b9b7a5637de0c4f7b3
		//String params = "from=8f20678b5396a977408d7f2e6d45d5d0&msg=Hello";
		String params = "id=448159d8-375a-4e05-b660-decdfa288004";
		ByteArrayInputStream is = new ByteArrayInputStream(params.getBytes("UTF-8"));
		byte[] data = request(RequestMethod.POST, "https://192.168.0.106/op/patient/information", DefaultProcess.write(is, 1024));
		System.out.println(HttpConnection.byteToString(data));
		is.close();
	}

	public static byte[] request(final RequestMethod method, final String url, HttpConnectionProcess<?>... processes) throws IOException {
		final Map<ProcessPoint, List<HttpConnectionProcess<?>>> processesMap = sortProcesses(processes);
		final HttpURLConnection connection = HttpConnection.getDefaultConnector(url, method);
		unCareReturnTypeProcess(processesMap, ProcessPoint.beforeConnect, connection);
		connection.connect();
		unCareReturnTypeProcess(processesMap, ProcessPoint.write, connection);
		byte[] data = null;
		List<HttpConnectionProcess<?>> read = processesMap.get(ProcessPoint.read);
		if (read != null && read.size() > 0) {
			HttpConnectionProcess<?> readOnce = read.get(0);
			Object result = readOnce.process(connection);
			if (result instanceof byte[]) {
				data = (byte[]) result;
			}
		} else {
			data = DefaultProcess.read().process(connection);
		}
		connection.disconnect();
		return data;
	}

	private static <V> Map<ProcessPoint, List<HttpConnectionProcess<?>>> sortProcesses(HttpConnectionProcess<?>... processes) {
		final Map<ProcessPoint, List<HttpConnectionProcess<?>>> processesMap = new HashMap<ProcessPoint, List<HttpConnectionProcess<?>>>();
		if (processes != null) {
			Arrays.stream(processes).forEach(process -> {
				ProcessPoint point = process.point();
				if (point != ProcessPoint.NONE) {
					List<HttpConnectionProcess<?>> pointList = processesMap.get(point);
					if (pointList == null) {
						pointList = new ArrayList<HttpConnectionProcess<?>>();
					}
					pointList.add(process);
					processesMap.put(point, pointList);
				}

			});
		}
		return processesMap;
	}

	private static void unCareReturnTypeProcess(Map<ProcessPoint, List<HttpConnectionProcess<?>>> processesMap, ProcessPoint point,
			final HttpURLConnection connection) throws IOException {
		List<HttpConnectionProcess<?>> processes = processesMap.get(point);
		if (processes != null && processes.size() > 0) {
			for (HttpConnectionProcess<?> p : processes) {
				try {
					p.process(connection);
				} catch (IOException e) {
					throw e;
				}
			}
		}
	}

}
