package com.utilities.vibal.utilities.models;

import android.content.Context;
import android.util.Log;

import com.utilities.vibal.utilities.activities.CashBoxManagerActivity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class CashBoxManager implements Serializable {
    private static final String TAG = "PruebaCashBoxItem";
    public static final String FILENAME ="cashBoxManager";
    public static final String FILENAME_TEMP ="cashBoxManagerTemp";
    private final List<CashBox> cashBoxes;

    public CashBoxManager(){
        cashBoxes = new ArrayList<>();
    }

    public CashBox get(int pos){
        return cashBoxes.get(pos);
    }

    public int size(){
        return cashBoxes.size();
    }

    public boolean isEmpty(){
        return cashBoxes.isEmpty();
    }

    /**
     * Adds a cashBox
     * @param cashBox cashBox to be added
     * @return 	true if this list doesn't contain the specified element
     */
    public boolean add(CashBox cashBox){
        if(!cashBoxes.contains(cashBox))
            return cashBoxes.add(cashBox);
        return false;
    }

    public boolean add(int index,CashBox cashBox){
        if(!cashBoxes.contains(cashBox)) {
            cashBoxes.add(index,cashBox);
            return true;
        }
        return false;
    }

    /**
     * Changes the name of an element
     * @param pos the position of the CashBox to be renamed
     * @param newName the new name of the CashBox
     * @return true if this list doesn't contain another CashBox with the same name
     * @throws IllegalArgumentException if the name is empty or its length exceeds CashBox.MAX_LENGTH_NAME
     */
    public boolean changeName (int pos, String newName) throws IllegalArgumentException {
        if(!cashBoxes.contains(new CashBox(newName))){
            cashBoxes.get(pos).setName(newName);
            return true;
        }
        return false;
    }

    public CashBox remove(int pos){
        return cashBoxes.remove(pos);
    }

    public void clear(){
        cashBoxes.clear();
    }

    public boolean set(int pos, CashBox cashBox){
        if(!cashBoxes.contains(cashBox)) {
            cashBoxes.set(pos,cashBox);
            return true;
        }
        return false;
    }

    public void changePosition(int oldPos, int newPos) {
        CashBox cashBox = cashBoxes.remove(oldPos);
        cashBoxes.add(newPos,cashBox);
    }

    public boolean duplicate(int index, String newName) throws IllegalArgumentException {
        CashBox cashBox = (CashBox) cashBoxes.get(index).clone();
        cashBox.setName(newName);
        return this.add(index+1,cashBox);
    }

//    public static CashBoxManager loadData(@NonNull Context context){
//        String fileName;
//        Log.d(TAG, "loadData: temp ult mod: "+ context.getFileStreamPath(FILENAME_TEMP).lastModified() + " ult mod original: " + context.getFileStreamPath(FILENAME).lastModified());
//        if(context.getFileStreamPath(FILENAME_TEMP).lastModified()>context.getFileStreamPath(FILENAME).lastModified()) {
//            Log.d(TAG, "loadData: coge el temp");
//            fileName = FILENAME_TEMP;
//        }
//        else
//            fileName = FILENAME;
//
//        try(ObjectInputStream objectInputStream = new ObjectInputStream(context.openFileInput(fileName))){
//            return (CashBoxManager) objectInputStream.readObject();
//        } catch (IOException | ClassNotFoundException e) {
//            Log.d(TAG, "loadData: error al leer archivo, crea nuevo CashBoxManager");
//            return new CashBoxManager();
//        }
//    }

//    public void saveDataTemp(Context context) {
//        try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(context.openFileOutput(FILENAME_TEMP,Context.MODE_PRIVATE))){
//            objectOutputStream.writeObject(this);
//            Log.i(TAG, "saveData: funciona");
//        } catch (IOException e) {
//            Log.i(TAG, "saveData: no");
//            e.printStackTrace();
//        }
//    }
}
