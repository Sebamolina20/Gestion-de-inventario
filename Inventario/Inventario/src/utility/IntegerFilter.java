package utility;

import javax.swing.text.*;

public class IntegerFilter extends DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (isValidInput(string)) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (isValidInput(text)) {
            super.replace(fb, offset, length, text, attrs);
        }
    }

    private boolean isValidInput(String text) {
        return text.matches("\\d+"); // Solo números enteros positivos
    }
}
