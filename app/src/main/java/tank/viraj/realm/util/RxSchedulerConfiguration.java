package tank.viraj.realm.util;

import android.os.Build;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.inject.Named;

import lombok.Getter;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Viraj Tank, 18-06-2016.
 */
@Getter
public class RxSchedulerConfiguration {
    private Scheduler subscribeOn;
    private Scheduler observeOn;

    public RxSchedulerConfiguration() {
        this.subscribeOn = getThreadPoolExecutor(coreNumber());
        this.observeOn = AndroidSchedulers.mainThread();
    }

    public RxSchedulerConfiguration(Scheduler subscribeOn, Scheduler observeOn) {
        this.subscribeOn = subscribeOn;
        this.observeOn = observeOn;
    }

    private Scheduler getThreadPoolExecutor(@Named("coreNumber") int coreNUmber) {
        /**
         * to get well-balanced computation performance we use: NumThread = NumCore + 1
         */
        return Schedulers.from(Executors.newFixedThreadPool(coreNUmber + 1));
    }

    private int coreNumber() {
        if (Build.VERSION.SDK_INT >= 17) {
            return Runtime.getRuntime().availableProcessors();
        }
        return getNumCoresOldPhones();
    }

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     *
     * @return The number of cores, or 1 if failed to get result
     */
    private int getNumCoresOldPhones() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                return Pattern.matches("cpu[0-9]+", pathname.getName());
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            //Default to return 1 core
            return 1;
        }
    }
}