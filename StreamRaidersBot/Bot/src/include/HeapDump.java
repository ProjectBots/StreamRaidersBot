package include;

import javax.management.MBeanServer;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class HeapDump {
	// This is the name of the HotSpot Diagnostic MBean
	private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";

	// field to store the hotspot diagnostic MBean
	private static volatile Object hotspotMBean;

	/**
	 * Call this method from your application whenever you want to dump the heap
	 * snapshot into a file.
	 *
	 * @param fileName name of the heap dump file
	 * @param live flag that tells whether to dump only the live objects
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public static void dumpHeap(String fileName, boolean live) throws ClassNotFoundException, IOException,
			NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		// initialize hotspot diagnostic MBean
		initHotspotMBean();
		Class<?> clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
		Method m = clazz.getMethod("dumpHeap", String.class, boolean.class);
		m.invoke(hotspotMBean, fileName, live);
	}

	// initialize the hotspot diagnostic MBean field
	private static void initHotspotMBean() throws ClassNotFoundException, IOException {
		if (hotspotMBean == null) {
			synchronized (HeapDump.class) {
				if (hotspotMBean == null) {
					hotspotMBean = getHotspotMBean();
				}
			}
		}
	}

	// get the hotspot diagnostic MBean from the
	// platform MBean server
	private static Object getHotspotMBean() throws ClassNotFoundException, IOException {
		Class<?> clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		Object bean = ManagementFactory.newPlatformMXBeanProxy(server, HOTSPOT_BEAN_NAME, clazz);
		return bean;
	}
}