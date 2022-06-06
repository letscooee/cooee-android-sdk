package com.letscooee.init;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.letscooee.BaseTestCase;
import org.junit.Ignore;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;

public class DefaultUserPropertiesCollectorTest extends BaseTestCase {

    private DefaultUserPropertiesCollector spyDefaultUserPropertiesCollector;

    @Override
    public void setUp() {
        super.setUp();
        DefaultUserPropertiesCollector defaultUserPropertiesCollector = new DefaultUserPropertiesCollector(context);
        spyDefaultUserPropertiesCollector = spy(defaultUserPropertiesCollector);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void get_location() {
        double[] output = new double[2];
        output[0] = 0.0;
        output[1] = 0.0;
        when(spyDefaultUserPropertiesCollector.getLocation()).thenReturn(output);
        double[] location = null;
        try {
            location = spyDefaultUserPropertiesCollector.getLocation();
            assertThat(location).isNotNull();
            assertThat(location.length).isGreaterThan(1);
            assertEquals(location[0], 0.0);
            assertEquals(location[1], 0.0);
        } catch (Exception e) {
            assertThat(location).isNotNull();
            assertThat(location.length).isGreaterThan(1);
        }
        verify(spyDefaultUserPropertiesCollector, times(1)).getLocation();
    }

    @Test
    public void get_network_data() {
        String[] output = new String[2];
        output[0] = "Operator";
        output[1] = "5G";
        when(spyDefaultUserPropertiesCollector.getNetworkData()).thenReturn(output);

        String[] networkData = null;
        try {
            networkData = spyDefaultUserPropertiesCollector.getNetworkData();
            assertThat(networkData).isNotNull();
            assertThat(networkData.length).isGreaterThan(1);
            assertEquals(networkData[0], "Operator");
            assertEquals(networkData[1], "5G");
        } catch (Exception e) {
            assertThat(networkData).isNotNull();
            assertThat(networkData.length).isGreaterThan(1);
        }

        verify(spyDefaultUserPropertiesCollector, times(1)).getNetworkData();
    }

    @Test
    public void is_bluetooth_on() {
        when(spyDefaultUserPropertiesCollector.isBluetoothOn()).thenReturn(true);
        boolean isBluetoothOn = false;
        try {
            isBluetoothOn = spyDefaultUserPropertiesCollector.isBluetoothOn();
            assertThat(isBluetoothOn).isTrue();
        } catch (Exception e) {
            assertThat(isBluetoothOn).isTrue();
        }
        verify(spyDefaultUserPropertiesCollector, times(1)).isBluetoothOn();
    }

    @Test
    public void is_connected_to_wifi() {
        when(spyDefaultUserPropertiesCollector.isConnectedToWifi()).thenReturn(true);
        boolean isConnectedToWifi = false;
        try {
            isConnectedToWifi = spyDefaultUserPropertiesCollector.isConnectedToWifi();
            assertThat(isConnectedToWifi).isTrue();
        } catch (Exception e) {
            assertThat(isConnectedToWifi).isTrue();
        }
        verify(spyDefaultUserPropertiesCollector, times(1)).isConnectedToWifi();
    }

    @Test
    public void get_available_internal_memory_size() {
        when(spyDefaultUserPropertiesCollector.getAvailableInternalMemorySize()).thenReturn(10L);
        long availableInternalMemorySize = 0;
        try {
            availableInternalMemorySize = spyDefaultUserPropertiesCollector.getAvailableInternalMemorySize();
            assertThat(availableInternalMemorySize).isEqualTo(10);
        } catch (Exception e) {
            assertThat(availableInternalMemorySize).isEqualTo(10);
        }
        verify(spyDefaultUserPropertiesCollector, times(1)).getAvailableInternalMemorySize();
    }

    @Test
    public void get_total_internal_memory_size() {
        when(spyDefaultUserPropertiesCollector.getTotalInternalMemorySize()).thenReturn(10L);
        long totalInternalMemorySize = 0;
        try {
            totalInternalMemorySize = spyDefaultUserPropertiesCollector.getTotalInternalMemorySize();
            assertThat(totalInternalMemorySize).isEqualTo(10);
        } catch (Exception e) {
            assertThat(totalInternalMemorySize).isEqualTo(10);
        }
        verify(spyDefaultUserPropertiesCollector, times(1)).getTotalInternalMemorySize();
    }

    @Test
    public void get_total_ram_memory_size() {
        when(spyDefaultUserPropertiesCollector.getTotalRAMMemorySize()).thenReturn(10.0);
        double totalRamMemorySize = 0.0;
        try {
            totalRamMemorySize = spyDefaultUserPropertiesCollector.getTotalRAMMemorySize();
            assertThat(totalRamMemorySize).isEqualTo(10);
        } catch (Exception e) {
            assertThat(totalRamMemorySize).isEqualTo(10);
        }
        verify(spyDefaultUserPropertiesCollector, times(1)).getTotalRAMMemorySize();
    }

    @Test
    public void get_available_ram_memoryS_size() {
        when(spyDefaultUserPropertiesCollector.getAvailableRAMMemorySize()).thenReturn(10.0);
        double availableRamMemorySize = 0.0;
        try {
            availableRamMemorySize = spyDefaultUserPropertiesCollector.getAvailableRAMMemorySize();
            assertThat(availableRamMemorySize).isEqualTo(10);
        } catch (Exception e) {
            assertThat(availableRamMemorySize).isEqualTo(10);
        }
        verify(spyDefaultUserPropertiesCollector, times(1)).getAvailableRAMMemorySize();
    }

    @Test
    public void get_device_orientation() {
        when(spyDefaultUserPropertiesCollector.getDeviceOrientation()).thenReturn("Portrait");
        String deviceOrientation = null;
        try {
            deviceOrientation = spyDefaultUserPropertiesCollector.getDeviceOrientation();
            assertThat(deviceOrientation).isEqualTo("Portrait");
        } catch (Exception e) {
            assertThat(deviceOrientation).isEqualTo("Portrait");
        }
        verify(spyDefaultUserPropertiesCollector, times(1)).getDeviceOrientation();
    }

    @Ignore("Failing due to adapter registration")
    @Test
    public void get_battery_info() {
        Map<String, Object> output = new HashMap<>();
        output.put("l", 10);
        output.put("c", true);
        when(spyDefaultUserPropertiesCollector.getBatteryInfo()).thenReturn(output);
        Map<String, Object> batteryInfo = null;
        try {
            batteryInfo = spyDefaultUserPropertiesCollector.getBatteryInfo();
            assertThat(batteryInfo).isNotNull();
            assertEquals(batteryInfo.get("l"), 10);
            assertEquals(batteryInfo.get("c"), true);
        } catch (Exception e) {
            assertThat(batteryInfo).isNotNull();
            assertEquals(batteryInfo.get("l"), 10);
            assertEquals(batteryInfo.get("c"), true);
        }

        verify(spyDefaultUserPropertiesCollector, times(1)).getBatteryInfo();
    }

    @Test
    public void get_screen_resolution() {
        when(spyDefaultUserPropertiesCollector.getScreenResolution()).thenReturn("1920x1080");
        String screenResolution = null;
        try {
            screenResolution = spyDefaultUserPropertiesCollector.getScreenResolution();
            assertThat(screenResolution).isEqualTo("1920x1080");
        } catch (Exception e) {
            assertThat(screenResolution).isEqualTo("1920x1080");
        }
        verify(spyDefaultUserPropertiesCollector, times(1)).getScreenResolution();
    }

    @Test
    public void get_dpi() {
        when(spyDefaultUserPropertiesCollector.getDpi()).thenReturn("1dpi");
        String dpi = null;
        try {
            dpi = spyDefaultUserPropertiesCollector.getDpi();
            assertThat(dpi).isEqualTo("1dpi");
        } catch (Exception e) {
            assertThat(dpi).isEqualTo("1dpi");
        }
        verify(spyDefaultUserPropertiesCollector, times(1)).getDpi();
    }

    @Test
    public void get_locale() {
        when(spyDefaultUserPropertiesCollector.getLocale()).thenReturn("en_US");
        String locale = null;
        try {
            locale = spyDefaultUserPropertiesCollector.getLocale();
            assertThat(locale).isEqualTo("en_US");
        } catch (Exception e) {
            assertThat(locale).isEqualTo("en_US");
        }
        verify(spyDefaultUserPropertiesCollector, times(1)).getLocale();
    }

    @Test
    public void get_installed_time() {
        String output = "2019-01-01T00:00:00.000Z";
        when(spyDefaultUserPropertiesCollector.getInstalledTime()).thenReturn(output);
        String installedTime = null;
        try {
            installedTime = spyDefaultUserPropertiesCollector.getInstalledTime();
            assertThat(installedTime).isEqualTo(output);
        } catch (Exception e) {
            assertThat(installedTime).isEqualTo(output);
        }
        verify(spyDefaultUserPropertiesCollector, times(1)).getInstalledTime();
    }

}