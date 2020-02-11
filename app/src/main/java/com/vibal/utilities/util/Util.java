package com.vibal.utilities.util;import android.app.DatePickerDialog;import android.content.Context;import android.content.Intent;import android.view.View;import android.view.inputmethod.InputMethodManager;import android.widget.EditText;import android.widget.TextView;import androidx.annotation.NonNull;import androidx.appcompat.app.AlertDialog;import com.vibal.utilities.R;import com.vibal.utilities.modelsNew.CashBox;import java.text.DateFormat;import java.text.DecimalFormatSymbols;import java.util.Calendar;import java.util.regex.Matcher;import java.util.regex.Pattern;public class Util {    private static final Pattern patternNonDigit = Pattern.compile("\\D");    /**     * Creates an alert dialog showing the help of the activity     *     * @param context            current context     * @param helpTitle          title for the help dialog     * @param helpResourceString message of the help dialog     * @return the help dialog     */    @NonNull    public static AlertDialog createHelpDialog(@NonNull Context context, int helpTitle, int helpResourceString) {        return new AlertDialog.Builder(context)                .setTitle(helpTitle)                .setMessage(helpResourceString)                .setPositiveButton(R.string.button_gotIt, null)                .create();    }    /**     * Shows the keyboard     *     * @param context  current context     * @param editText EditText which will receive the focus     */    public static void showKeyboard(@NonNull Context context, @NonNull EditText editText) {        InputMethodManager input = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);        editText.postDelayed(() -> {            editText.requestFocus();            if (input != null) {                input.showSoftInput(editText, 0);            }        }, 100);    }    @NonNull    public static Intent getShareIntent(@NonNull CashBox cashBox) {        Intent shareIntent = new Intent(Intent.ACTION_SEND);        shareIntent.putExtra(Intent.EXTRA_TEXT, cashBox.toString())                .setType("text/plain");        return shareIntent;    }    public static double parseDouble(@NonNull String str) throws NumberFormatException {        // not a static final variable in case you change config mid-app        char decimalSeparator = DecimalFormatSymbols.getInstance().getMonetaryDecimalSeparator();        Matcher matcher = patternNonDigit.matcher(str);        StringBuilder modifiedString = new StringBuilder(str);        char temp;        while (matcher.find()) {            temp = str.charAt(matcher.start());            if (temp == decimalSeparator)                modifiedString.setCharAt(matcher.start(), '.');            else if (temp != '.' && temp != '-' && temp != '+')                throw new NumberFormatException("Multiple decimal separator");        }        return Double.parseDouble(modifiedString.toString());    }    // 2 + 6 * 8 + 4    // 2 + 6 * 8    public static double parseExpression(@NonNull String expression) throws NumberFormatException {        char decimalSeparator = DecimalFormatSymbols.getInstance().getMonetaryDecimalSeparator();        String groupDecimalSeparator = "[." + decimalSeparator + "]";        // Multiplication and division first        // (\d+(?:[.,]\d+)?)([*/])(\d+(?:[.,]\d+)?)        Pattern pattern = Pattern.compile("(\\d+(?:" + groupDecimalSeparator +                "\\d+)?)([*/])(\\d+(?:" + groupDecimalSeparator + "\\d+)?)");        StringBuilder stringBuilder = new StringBuilder(expression);        Matcher matcher = pattern.matcher(stringBuilder);        int start = 0;        double res;        while (matcher.find(start)) {            LogUtil.debug("PruebaUtil", "Mult pos: " + matcher.start() + "\n1: " +                    matcher.group(1) + "\n2: " + matcher.group(2) + "\n3: " + matcher.group(3));            start = matcher.start();            res = calculateOperation(matcher.group(2).charAt(0), parseDouble(matcher.group(1)),                    parseDouble(matcher.group(3)));            matcher = pattern.matcher(stringBuilder.replace(start, matcher.end(), Double.toString(res)));        }        // Sum and substraction second        // (\d+(?:[.,]\d+)?)([+-])(\d+(?:[.,]\d+)?)        pattern = Pattern.compile("(\\d+(?:" + groupDecimalSeparator + "\\d+)?)([+-])(\\d+(?:" +                groupDecimalSeparator + "\\d+)?)");        matcher = pattern.matcher(stringBuilder);        start = 0;        while (matcher.find(start)) {            LogUtil.debug("PruebaUtil", "Sum pos: " + matcher.start() + "\n1: " +                    matcher.group(1) + "\n2: " + matcher.group(2) + "\n3: " + matcher.group(3));            // Start in 1 means that 1st char is a minus sign            start = matcher.start() == 1 ? 0 : matcher.start();            res = calculateOperation(matcher.group(2).charAt(0),                    parseDouble(stringBuilder.substring(start, matcher.end(1))),                    parseDouble(matcher.group(3)));            matcher = pattern.matcher(stringBuilder.replace(start, matcher.end(), Double.toString(res)));        }        return parseDouble(stringBuilder.toString());//        Matcher matcher = patternOperators.matcher(expression);//        int start = 0;//        char operator = '+';//        double result = 0;////        while (matcher.find(1)) { //me salto el primero ya que puede ser un signo//            if (start == matcher.start()) //two operators together isn't allowed//                throw new NumberFormatException("Illegal expression");//            result = calculateOperation(operator, result,//                    parseDouble(expression.substring(start, matcher.start())));//            operator = expression.charAt(matcher.start());//            start = matcher.end();//        }//        return calculateOperation(operator, result, parseDouble(expression.substring(start)));    }    public static double roundTwoDecimals(double num) {        return ((double) Math.round(num * 100)) / 100;    }    private static double calculateOperation(char operator, double num1, double num2) {        if (operator == '+')            return num1 + num2;        else if (operator == '-')            return num1 - num2;        else if (operator == '*')            return num1 * num2;        else if (operator == '/') //it has to be / since it's the only one left            return num1 / num2;        else            return num1;    }    public static class TextViewDatePickerClickListener implements View.OnClickListener {        private Calendar calendar;        private DatePickerDialog dialog;        private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);        public TextViewDatePickerClickListener(@NonNull Context context, @NonNull TextView textView,                                               Calendar date, boolean allowPast) {            calendar = (Calendar) date.clone();            Calendar current = Calendar.getInstance();            int currentDay = current.get(Calendar.DAY_OF_MONTH);            int currentMonth = current.get(Calendar.MONTH);            int currentYear = current.get(Calendar.YEAR);            textView.setText(dateFormat.format(calendar.getTime()));            dialog = new DatePickerDialog(context, (datePicker, year, month, dayOfMonth) -> {                calendar.set(year, month, dayOfMonth);                textView.setText(dateFormat.format(calendar.getTime()));                LogUtil.debug("Prueba", Long.toString(getDaysFromCurrent()));            }, currentYear, currentMonth, currentDay);            if (!allowPast)                dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());        }        public TextViewDatePickerClickListener(@NonNull Context context, @NonNull TextView textView,                                               boolean allowPast) {            this(context,textView,Calendar.getInstance(),allowPast);        }        public Calendar getCalendar() {            return calendar;        }        /**         * Computes the days between the chosen date and the current date         *         * @return days difference         */        public long getDaysFromCurrent() {            double diff = calendar.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();            return (long) Math.ceil(diff / 1000 / 60 / 60 / 24); // ceil from millis to days        }        @Override        public void onClick(View view) {            dialog.show();        }    }}