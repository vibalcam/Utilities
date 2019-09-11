package com.utilities.vibal.utilities.util;import android.content.Context;import android.content.Intent;import android.view.inputmethod.InputMethodManager;import android.widget.EditText;import androidx.annotation.NonNull;import androidx.appcompat.app.AlertDialog;import com.utilities.vibal.utilities.R;import com.utilities.vibal.utilities.models.CashBox;import java.text.DecimalFormatSymbols;import java.text.ParseException;import java.util.regex.Matcher;import java.util.regex.Pattern;public class Util {    private static final Pattern patternDecimalSeparator = Pattern.compile("\\D");    private static final Pattern patternOperators = Pattern.compile("[+\\-*/]");    /**     * Creates an alert dialog showing the help of the activity     *     * @param context            current context     * @param helpTitle          title for the help dialog     * @param helpResourceString message of the help dialog     * @return the help dialog     */    public static AlertDialog getHelpDialog(Context context, int helpTitle, int helpResourceString) {        AlertDialog.Builder builder = new AlertDialog.Builder(context);        builder.setTitle(helpTitle)                .setMessage(helpResourceString)                .setPositiveButton(R.string.button_gotIt, null);        AlertDialog dialog = builder.create();        dialog.setCanceledOnTouchOutside(false);        return dialog;    }    /**     * Shows the keyboard     *     * @param context  current context     * @param editText EditText which will receive the focus     */    public static void showKeyboard(@NonNull Context context, EditText editText) {        InputMethodManager input = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);        editText.postDelayed(() -> {            editText.requestFocus();            input.showSoftInput(editText, 0);        }, 100);    }    //TODO get cashBox or delete/move to item    public static Intent getShareIntent(CashBox cashBox) {        Intent shareIntent = new Intent(Intent.ACTION_SEND);        shareIntent.putExtra(Intent.EXTRA_TEXT, cashBox.toString())                .setType("text/plain");        return shareIntent;    }    public static double parseDouble(String str) throws NumberFormatException {        // not a static final variable in case you change config mid-app        //TODO method for update in case of config change        char decimalSeparator = DecimalFormatSymbols.getInstance().getMonetaryDecimalSeparator();        Matcher matcher = patternDecimalSeparator.matcher(str);        StringBuilder modifiedString = new StringBuilder(str);        char temp;        while (matcher.find()) {            temp = str.charAt(matcher.start());            if (temp == decimalSeparator)                modifiedString.setCharAt(matcher.start(), '.');            else if(temp!='.' && temp!='-')                throw new NumberFormatException("Multiple decimal separator");        }        return Double.parseDouble(modifiedString.toString());    }    //todo change res to allow    public static double parseExpression(String expression) throws NumberFormatException {        Matcher matcher = patternOperators.matcher(expression);        int start = 0;        char operator = '+';        double result = 0;        while(matcher.find()) {            if(start == matcher.start()) //two operators together isn't allowed                throw new NumberFormatException("Illegal expression");            result = calculateOperation(operator,result,                    parseDouble(expression.substring(start,matcher.start())));            operator = expression.charAt(matcher.start());            start = matcher.end();        }        return calculateOperation(operator,result,parseDouble(expression.substring(start)));    }    private static double calculateOperation(char operator, double num1, double num2) {        if(operator=='+')            return num1 += num2;        else if(operator=='-')            return num1-=num2;        else if(operator=='*')            return num1*=num2;        else if(operator=='/') //it has to be / since it's the only one left            return num1/=num2;        else            return num1;    }}