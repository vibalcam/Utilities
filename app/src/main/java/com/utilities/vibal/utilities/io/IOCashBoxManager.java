package com.utilities.vibal.utilities.io;

import android.content.Context;

import androidx.annotation.NonNull;

import com.utilities.vibal.utilities.models.CashBoxManager;
import com.utilities.vibal.utilities.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class IOCashBoxManager {
    private static final String TAG = "PruebaIO";
    private static final String FILENAME = "cashBoxManager";
    private static final String FILENAME_TEMP = "cashBoxManagerTemp";

    public static void saveCashBoxManagerTemp(CashBoxManager manager, Context context) throws IOException {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(context.openFileOutput(FILENAME_TEMP, Context.MODE_PRIVATE))) {
            objectOutputStream.writeObject(manager);
            LogUtil.debug(TAG, "saveData: funciona");
        } catch (IOException e) {
            LogUtil.error(TAG, "saveCashBoxManagerTemp: ", e);
            throw e;
        }
    }

    /**
     * Renames a File
     *
     * @param originalFileName name of the original file
     * @param newFileName      new name of the file
     * @param context          context in which it is being called
     * @return true if and only if the renaming succeeded; false otherwise
     */
    public static boolean renameFile(String originalFileName, String newFileName, @NonNull Context context) {
        File originalFile = context.getFileStreamPath(originalFileName);
        if (originalFile.exists()) {
            File newFile = new File(originalFile.getParent(), newFileName);
            if (newFile.exists())
                context.deleteFile(newFile.getName());

            return originalFile.renameTo(newFile);
        } else
            return false;
    }

    public static boolean renameCashBoxManagerTemp(Context context) {
        return renameFile(FILENAME_TEMP, FILENAME, context);
    }

    public static CashBoxManager loadCashBoxManager(@NonNull Context context) {
        String fileName;
        LogUtil.debug(TAG, "loadData: temp ult mod: " + context.getFileStreamPath(FILENAME_TEMP).lastModified() + " ult mod original: " + context.getFileStreamPath(FILENAME).lastModified());
        if (context.getFileStreamPath(FILENAME_TEMP).lastModified() > context.getFileStreamPath(FILENAME).lastModified()) {
            LogUtil.debug(TAG, "loadData: coge el temp");
            fileName = FILENAME_TEMP;
        } else
            fileName = FILENAME;

        Object cashBoxManager;
        try (ObjectInputStream objectInputStream = new ObjectInputStream(context.openFileInput(fileName))) {
            cashBoxManager = objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LogUtil.error(TAG, "loadData: error al leer archivo, crea nuevo CashBoxManager", e);
            return new CashBoxManager();
        }
        if (cashBoxManager instanceof CashBoxManager)
            return (CashBoxManager) cashBoxManager;
        else {
            LogUtil.debug(TAG, "loadData: error al instanceof, crea nuevo CashBoxManager");
            return new CashBoxManager();
        }
    }
}
