package com.vibal.utilities.ui.cashBoxManager;

import androidx.annotation.IntDef;

import com.vibal.utilities.backgroundTasks.ReminderReceiver;
import com.vibal.utilities.persistence.repositories.CashBoxLocalRepository;
import com.vibal.utilities.persistence.repositories.CashBoxOnlineRepository;
import com.vibal.utilities.persistence.repositories.CashBoxRepository;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface CashBoxType {
    int LOCAL = 0;
    int ONLINE = 1;

    @IntDef({ONLINE, LOCAL})
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
    }

    @ReminderReceiver.ReminderType
    String getReminderType();

    @Type
    int getCashBoxType();

    static Class<? extends CashBoxRepository> getCashBoxRepositoryClass(@Type int cashBoxType) {
        switch (cashBoxType) {
            case LOCAL:
                return CashBoxLocalRepository.class;
            case ONLINE:
                return CashBoxOnlineRepository.class;
            default:
                throw new IllegalArgumentException("Should never happen");
        }
    }

    interface LOCAL extends CashBoxType {
        @Override
        default String getReminderType() {
            return ReminderReceiver.LOCAL;
        }

        @Override
        default int getCashBoxType() {
            return LOCAL;
        }
    }

    interface ONLINE extends CashBoxType {
        @Override
        default String getReminderType() {
            return ReminderReceiver.ONLINE;
        }

        @Override
        default int getCashBoxType() {
            return ONLINE;
        }
    }
}
