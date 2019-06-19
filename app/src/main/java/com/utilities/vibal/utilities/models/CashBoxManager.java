package com.utilities.vibal.utilities.models;

import java.util.List;

public class CashBoxManager {
    private List<CashBox> cashBoxes;

    /**
     * Adds a cashBox
     *
     * @param cashBox cashBox to be added
     * @return true if this list doesn't contain the specified element
     */
    public boolean add(CashBox cashBox) {
        if (!cashBoxes.contains(cashBox))
            return cashBoxes.add(cashBox);
        return false;
    }

    public boolean add(int index, CashBox cashBox) {
        if (!cashBoxes.contains(cashBox)) {
            cashBoxes.add(index, cashBox);
            return true;
        }
        return false;
    }

    /**
     * Changes the name of an element
     *
     * @param pos     the position of the CashBox to be renamed
     * @param newName the new name of the CashBox
     * @return true if this list doesn't contain another CashBox with the same name
     * @throws IllegalArgumentException if the name is empty or its length exceeds CashBox.MAX_LENGTH_NAME
     */
    public boolean changeName(int pos, String newName) throws IllegalArgumentException {
        int index = cashBoxes.indexOf(new CashBox(newName));
        if (index == -1) {
            cashBoxes.get(pos).setName(newName);
            return true;
        } else
            return index == pos;
    }

    public CashBox remove(int pos) {
        return cashBoxes.remove(pos);
    }

    public void clear() {
        cashBoxes.clear();
    }

    public boolean set(int pos, CashBox cashBox) {
        if (!cashBoxes.contains(cashBox)) {
            cashBoxes.set(pos, cashBox);
            return true;
        }
        return false;
    }

    public void move(int oldPos, int newPos) throws IndexOutOfBoundsException {
        if (oldPos < 0 || newPos >= cashBoxes.size())
            throw new IndexOutOfBoundsException("Trying to move from " + oldPos + " to " + newPos);
        else if (oldPos != newPos) {   // if both are the same position, no need to move
            CashBox cashBox = cashBoxes.remove(oldPos);
            cashBoxes.add(newPos, cashBox);
        }
    }

    public boolean duplicate(int index, String newName) throws IllegalArgumentException {
//        try {
            CashBox cashBox = cashBoxes.get(index).clone();
            cashBox.setName(newName);
            return this.add(index + 1, cashBox);
//        } catch (CloneNotSupportedException e) {
//            LogUtil.error("PruebaCashBoxManager", "cloning: ", e);
//            return false;
//        }
    }
}
