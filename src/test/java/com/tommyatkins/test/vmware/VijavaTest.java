package com.tommyatkins.test.vmware;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vmware.vim25.CustomizationAdapterMapping;
import com.vmware.vim25.CustomizationFixedIp;
import com.vmware.vim25.CustomizationFixedName;
import com.vmware.vim25.CustomizationGlobalIPSettings;
import com.vmware.vim25.CustomizationGuiUnattended;
import com.vmware.vim25.CustomizationIPSettings;
import com.vmware.vim25.CustomizationIdentification;
import com.vmware.vim25.CustomizationLinuxOptions;
import com.vmware.vim25.CustomizationLinuxPrep;
import com.vmware.vim25.CustomizationName;
import com.vmware.vim25.CustomizationSpec;
import com.vmware.vim25.CustomizationSpecInfo;
import com.vmware.vim25.CustomizationSpecItem;
import com.vmware.vim25.CustomizationUserData;
import com.vmware.vim25.CustomizationVirtualMachineName;
import com.vmware.vim25.ManagedEntityStatus;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecFileOperation;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.CustomizationSpecManager;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VijavaTest {

	public static void main(String[] args) throws Exception {
		// "vm-926292c1-a228-46ed-a181-722f987b74e4" 7.8
		String host = "192.168.6.30";
//		String host = "192.168.7.2";
		URL url = new URL("https", host, "/sdk");
		ServiceInstance instance = new ServiceInstance(url, "administrator", "Rimi123456", true);
//		ServiceInstance instance = new ServiceInstance(url, "root", "rimitest67510", true);
		// test(instance);
		createMin(instance);
		// CustomizationSpec elearningCustomizations = getCustomizationSpec(instance, "E-LearningCustomizations");

	}

	public static void test(ServiceInstance instance) throws Exception {

		Folder rootFolder = instance.getRootFolder();

		InventoryNavigator inventoryNavigator = new InventoryNavigator(rootFolder);
		ManagedEntity[] entities = inventoryNavigator.searchManagedEntities("Folder");

		for (ManagedEntity managedEntity : entities) {
			System.out.println("####################################");
			System.out.println(managedEntity.getClass().getName());
			System.out.println(managedEntity.getName());
			System.out.println("####################################");
		}

	}

	public static CustomizationSpec createCustomizations() {
		CustomizationSpec spec = new CustomizationSpec();

		// 创建文件类型Linux
		CustomizationLinuxOptions options = new CustomizationLinuxOptions();
		// CustomizationWinOptions options = new CustomizationWinOptions();

		spec.setOptions(options);

		// 创建应答文件
		CustomizationLinuxPrep linuxPrep = new CustomizationLinuxPrep();

		linuxPrep.setDomain("localhost");
		linuxPrep.setHwClockUTC(true);
		linuxPrep.setTimeZone("Asia/Hong_Kong");
		CustomizationFixedName hostName = new CustomizationFixedName();
		hostName.setName("E-Learning-7-10");
		linuxPrep.setHostName(hostName);

		spec.setIdentity(linuxPrep);

		CustomizationGlobalIPSettings globalIPSettings = new CustomizationGlobalIPSettings();
		spec.setGlobalIPSettings(globalIPSettings);

		// 设置IP地址
		CustomizationAdapterMapping[] adapterMappings = new CustomizationAdapterMapping[1];
		CustomizationIPSettings ipSetting = new CustomizationIPSettings();
		// 重新设置自定义规范 IP 地址
		// 设置dns

		ipSetting.setDnsServerList(new String[] { "8.8.8.8" });
		// 设置gateway
		ipSetting.setGateway(new String[] { "192.168.7.1" });
		// 设置子网掩码
		ipSetting.setSubnetMask("255.255.255.0");
		// 设置IP地址
		CustomizationFixedIp ip = new CustomizationFixedIp();
		ip.setIpAddress("192.168.7.10");
		ipSetting.setIp(ip);

		CustomizationAdapterMapping adapter = new CustomizationAdapterMapping();
		adapter.setAdapter(ipSetting);

		adapterMappings[0] = adapter;

		spec.setNicSettingMap(adapterMappings);

		// CustomizationSpecItem customizationSpecItem = new CustomizationSpecItem();
		// customizationSpecItem.setSpec(spec);
		// CustomizationSpecInfo info = new CustomizationSpecInfo();
		// info.setName("CustomizationSpecInfo.name");
		// info.setDescription("CustomizationSpecInfo.description");
		// customizationSpecItem.setInfo(info);

		return spec;
	}

	public static void createMin(ServiceInstance instance) throws Exception {

		String templatename = "E-Learning-ubuntu-14.04.1-Template";
		// String templatename = "win7-ganjing-muban";
		String virtualmachinename = "E-Learning-7-10";
		String datastorename = "VM-kaifabu_Server";
		String poolname = "E-Learning-ubuntu";
		String folderName = "E-Learning_VMs";

		Folder rootFolder = instance.getRootFolder();

		InventoryNavigator inventoryNavigator = new InventoryNavigator(rootFolder);

		VirtualMachine templateVM = (VirtualMachine) inventoryNavigator.searchManagedEntity("VirtualMachine", templatename);
		Datastore datastore = (Datastore) inventoryNavigator.searchManagedEntity("Datastore", datastorename);
		ResourcePool pool = (ResourcePool) inventoryNavigator.searchManagedEntity("ResourcePool", poolname);
		ComputeResource computerResource = (ComputeResource) inventoryNavigator.searchManagedEntity("ComputeResource", "bosh_jq");
		Folder folder = (Folder) inventoryNavigator.searchManagedEntity("Folder", folderName);

		VirtualMachineRelocateSpec virtualMachineRelocateSpec = new VirtualMachineRelocateSpec();
		virtualMachineRelocateSpec.setDatastore(datastore.getMOR());
		virtualMachineRelocateSpec.setPool(pool.getMOR());
		virtualMachineRelocateSpec.setHost(computerResource.getHosts()[0].getMOR());

		// 虚拟机CPU和内存配置信息
		// VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
		// 设置CPU核数
		// configSpec.setNumCPUs(2);
		// 设置内存大小
		// long memory = 1024;

		// configSpec.setMemoryMB(memory);
		// 设置虚拟机名称
		// configSpec.setName(virtualmachinename);
		// 设置虚拟机描述
		// configSpec.setAnnotation("VirtualMachine Annotation");

		// 虚拟机克隆规范
		VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
		cloneSpec.setLocation(virtualMachineRelocateSpec);
		cloneSpec.setPowerOn(true);
		cloneSpec.setTemplate(false);
		// cloneSpec.setConfig(configSpec);

		// 自定义规范
		CustomizationSpec customizationSpec = createCustomizations();
		cloneSpec.setCustomization(customizationSpec);

		Task task = templateVM.cloneVM_Task(folder, virtualmachinename, cloneSpec);
		String result = task.waitForTask();
		if (result.equals(Task.SUCCESS)) {
			System.out.println("模板生成虚拟机成功");
		} else {
			System.out.println("模板生成虚拟机失败，请查询Vcenter 上相关日志信息");
		}
	}

	// 查询用户已经创建的自定义规范
	public static CustomizationSpec getCustomizationSpec(ServiceInstance instance, String customizationName) {
		CustomizationSpec customizationSpec = null;
		CustomizationSpecItem customizationSpecItem = null;
		com.vmware.vim25.mo.CustomizationSpecManager manager = instance.getCustomizationSpecManager();
		CustomizationSpecInfo[] infos = manager.getInfo();
		if (infos != null && infos.length > 0) {
			for (CustomizationSpecInfo info : infos) {
				if (info.getName().equalsIgnoreCase(customizationName)) {
					try {
						customizationSpecItem = manager.getCustomizationSpec(customizationName);
						customizationSpec = customizationSpecItem.getSpec();
					} catch (Exception e) {
						e.printStackTrace();
						return customizationSpec;
					}

				}
			}
		}
		CustomizationLinuxPrep identity = (CustomizationLinuxPrep) customizationSpec.getIdentity();
		String domain = identity.getDomain();
		System.out.println(domain);
		CustomizationFixedName hostName = (CustomizationFixedName) identity.getHostName();
		System.out.println(hostName.getName());
		System.out.println(identity.getTimeZone());
		System.out.println(identity.getHwClockUTC());

		return customizationSpec;
	}

	public static List<Object> getNetcard(ServiceInstance instance, String customizationName) {
		List<Object> list = new ArrayList<Object>();
		CustomizationSpec customizationSpec = null;
		CustomizationSpecItem customizationSpecItem = null;
		CustomizationSpecManager manager = instance.getCustomizationSpecManager();
		CustomizationSpecInfo[] infos = manager.getInfo();
		if (infos != null && infos.length > 0) {
			for (CustomizationSpecInfo info : infos) {
				if (info.getName().equalsIgnoreCase(customizationName)) {
					try {
						customizationSpecItem = manager.getCustomizationSpec(customizationName);
						customizationSpec = customizationSpecItem.getSpec();
						CustomizationAdapterMapping[] adapterMappings = customizationSpec.nicSettingMap;
						if (adapterMappings != null && adapterMappings.length > 0) {
							for (CustomizationAdapterMapping adapter : adapterMappings) {
								CustomizationIPSettings ipSettings = adapter.adapter;
								// IP地址疑惑
								CustomizationFixedIp fixedIp = (CustomizationFixedIp) ipSettings.ip;
								System.out.println(fixedIp.ipAddress);// IP地址

								System.out.println(ipSettings.subnetMask);// subnetMask
								String[] gateways = ipSettings.gateway;
								if (gateways != null && gateways.length > 0) {
									for (String str : gateways) {
										if (!str.equalsIgnoreCase("")) {
											System.out.println(str);// gateway
										}
									}
								}
								String[] dnss = ipSettings.dnsServerList;
								if (dnss != null && dnss.length > 0) {
									for (String dns : dnss) {
										System.out.println(dns); // dnsServer
									}
								}

							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						return list;
					}

				}
			}
		}
		return list;
	}

	public static void create() throws MalformedURLException, RemoteException {
		String host = "192.168.6.30";
		URL url = new URL("https", host, "/sdk");

		String templatename = "ubuntu-64";
		String poolname = "E-learning";
		String hostname = "";
		String virtualmachinename = "vm_vijava_test";
		String datastorename = "VMware-JAVA/E-Learning";
		long disksizekb = 10 * 1024 * 1024;
		String diskmode = "persistent";

		ServiceInstance instance = null;
		VirtualMachine templateVM = null;
		ResourcePool pool = null;
		Datastore datastore = null;
		ComputeResource computerResource = null;
		InventoryNavigator inventoryNavigator = null;
		Task task = null;

		instance = new ServiceInstance(url, "administrator", "Rimi123456", true);

		Folder rootFolder = instance.getRootFolder();

		inventoryNavigator = new InventoryNavigator(rootFolder);

		try {
			templateVM = (VirtualMachine) inventoryNavigator.searchManagedEntity("VirtualMachine", templatename);

			if (templateVM != null) {
				System.out.println("template 查询成功");
			} else {
				System.out.println("template 查询失败，请仔细检查配置模板是否存在");
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out.println("虚拟机模板文件存在问题:" + e.getMessage());
		}

		VirtualMachineRelocateSpec virtualMachineRelocateSpec = new VirtualMachineRelocateSpec();

		try {
			datastore = (Datastore) inventoryNavigator.searchManagedEntity("Datastore", datastorename);
			virtualMachineRelocateSpec.setDatastore(datastore.getMOR());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("指定Datastore存在问题:" + e.getMessage());
		}

		if (null != poolname && !"".equals(poolname)) {
			try {
				pool = (ResourcePool) inventoryNavigator.searchManagedEntity("ResourcePool", poolname);
				if (pool != null) {
					System.out.println("pool 查询成功");
					virtualMachineRelocateSpec.setPool(pool.getMOR());
				} else {
					System.out.println("pool 查询失败，请仔细检查配置资源池是否存在");
				}

			} catch (RemoteException e) {
				System.out.println("Vcenter资源池存在问题:" + e.getMessage());

			}

		} else {
			try {
				computerResource = (ComputeResource) inventoryNavigator.searchManagedEntity("ComputeResource", hostname);
				if (computerResource != null) {
					if (computerResource.getResourcePool() != null) {
						virtualMachineRelocateSpec.setPool(computerResource.getResourcePool().getMOR());
					}
					virtualMachineRelocateSpec.setHost(computerResource.getHosts()[0].getMOR());
				} else {
					System.out.println("Esxi 查询失败，请仔细检查配置Esxi是否存在");
				}

			} catch (RemoteException e) {
				e.printStackTrace();
				System.out.println("Vcenter下Esxi存在问题:" + e.getMessage());
			}

		}

		// 虚拟机CPU和内存配置信息
		VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
		// 设置CPU核数
		configSpec.setNumCPUs(2);
		// 设置内存大小
		long memory = 1024;
		configSpec.setMemoryMB(memory);
		// 设置虚拟机名称
		configSpec.setName(virtualmachinename);
		// 设置虚拟机描述
		configSpec.setAnnotation("VirtualMachine Annotation");

		// 更改磁盘大小
		VirtualDeviceConfigSpec diskSpec = createDiskSpec(templateVM, "local02-2", disksizekb, diskmode);
		if (diskSpec != null) {
			System.out.println("创建disk不为空");
		} else {
			System.out.println("创建disk为空");
		}

		VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
		cloneSpec.setLocation(virtualMachineRelocateSpec);
		cloneSpec.setPowerOn(true);
		cloneSpec.setTemplate(false);
		cloneSpec.setConfig(configSpec);

		try {

			task = templateVM.cloneVM_Task((Folder) templateVM.getParent(), virtualmachinename, cloneSpec);
			String result = task.waitForTask();
			if (result.equals(Task.SUCCESS)) {
				System.out.println("模板生成虚拟机成功");
			} else {
				System.out.println("模板生成虚拟机失败，请查询Vcenter 上相关日志信息");
			}

		} catch (RemoteException e) {
			System.out.println("创建任务失败:" + e.getMessage());
			if (instance != null) {
				instance.getServerConnection().logout();
				System.out.println("存在异常:" + e.getMessage());
			}

		} catch (InterruptedException e) {
			System.out.println("创建任务失败:" + e.getMessage());
			e.printStackTrace();
		}
	}

	public static VirtualDeviceConfigSpec createDiskSpec(VirtualMachine vm, String dsName, long diskSizeKB, String diskMode) {
		// 虚拟磁盘配置信息
		VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();

		VirtualMachineConfigInfo vmConfig = (VirtualMachineConfigInfo) vm.getConfig();
		VirtualDevice[] vds = vmConfig.getHardware().getDevice();
		// 虚拟磁盘相关操作
		diskSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
		diskSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.create);

		VirtualDisk vd = new VirtualDisk();
		vd.setCapacityInKB(diskSizeKB);
		diskSpec.setDevice(vd);
		vd.setKey(0);
		vd.setUnitNumber(0); // 修改虚拟磁盘大小，主要就是修改已存在的SCSI 控制器，并重新设置VirtualDisk.setCapacityInKB().
		int key = 0;
		for (int k = 0; k < vds.length; k++) {
			if (vds[k].getDeviceInfo().getLabel().equalsIgnoreCase("SCSI Controller 0")) {
				key = vds[k].getKey();
			}
		}
		vd.setControllerKey(key);

		VirtualDiskFlatVer2BackingInfo diskfileBacking = new VirtualDiskFlatVer2BackingInfo();
		String fileName = "[" + dsName + "]";
		diskfileBacking.setFileName(fileName);
		diskfileBacking.setDiskMode(diskMode);
		diskfileBacking.setThinProvisioned(true);
		vd.setBacking(diskfileBacking);
		return diskSpec;
	}
}
