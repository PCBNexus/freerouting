package app.freerouting.gui;

import app.freerouting.logger.FRLogger;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

// Singleton class to manage the text resources for the application
public class TextManager {
  private Locale currentLocale;
  private String currentBaseName;
  private ResourceBundle messages;
  private Font materialDesignIcons = null;
  // A key-value pair for icon names and their corresponding unicode characters
  private Map<String, Integer> iconMap = new HashMap<>()
  {{
    put("auto-fix", 0xF0068);
    put("undo", 0xF054C);
    put("redo", 0xF044E);
    put("alert", 0xF0026);
    put("close-octagon", 0xF015C);
  }};

  public TextManager(Class baseClass, Locale locale) {
    this.currentLocale = locale;
    loadResourceBundle(baseClass.getName());

    try
    {
      // Load the font
      materialDesignIcons = Font.createFont(Font.TRUETYPE_FONT, GlobalSettings.class.getResourceAsStream("/materialdesignicons-webfont.ttf"));

      // Register the font
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      ge.registerFont(materialDesignIcons);
    } catch (IOException | FontFormatException e)
    {
      FRLogger.error("There was a problem loading the Material Design Icons font", e);
    }
  }

  private void loadResourceBundle(String baseName) {
    this.currentBaseName = baseName;
    this.messages = ResourceBundle.getBundle(currentBaseName, currentLocale);
  }

  public void setLocale(Locale locale) {
    this.currentLocale = locale;
    loadResourceBundle(currentBaseName);
  }

  public String getText(String key, String... args) {
    // if the key is not found, return an empty string
    if (!messages.containsKey(key)) {
      return key;
    }

    String text = messages.getString(key);

    // Pattern to match {{variable_name}} placeholders
    Pattern pattern = Pattern.compile("\\{\\{(.+?)\\}\\}");
    Matcher matcher = pattern.matcher(text);

    // Find and replace all matches
    int argIndex = 0;
    while (matcher.find()) {
      // Entire match including {{ and }}
      String placeholder = matcher.group(0);

      if (!placeholder.startsWith("{{icon:") && argIndex < args.length)
      {
        // replace the placeholder with the value
        text = text.replace(placeholder, args[argIndex]);
        argIndex++;
      }
    }

    return text;
  }

  private String insertIcons(JComponent component, String text) {
    // Pattern to match {{variable_name}} placeholders
    Pattern pattern = Pattern.compile("\\{\\{icon:(.+?)\\}\\}");
    Matcher matcher = pattern.matcher(text);

    // Find all matches
    while (matcher.find()) {
      // Entire match including {{ and }}
      String placeholder = matcher.group(0);

      // Get the icon name
      String iconName = matcher.group(1);

      try {

        // Get the unicode code point for the icon
        int codePoint = iconMap.get(iconName);

        // Convert the code point to a String
        text = text.replace(placeholder, new String(Character.toChars(codePoint)));

        Font originalFont = component.getFont();
        component.setFont(materialDesignIcons.deriveFont(Font.PLAIN, originalFont.getSize() * 1.5f));
      } catch (Exception e) {
        FRLogger.error("There was a problem setting the icon for the component", e);
      }
    }

    return text;
  }

  // Add methods to set text for different GUI components
  public void setText(JComponent component, String key, String... args) {
    String text = getText(key, args);
    String tooltip = getText(key + "_tooltip", args);

    if (tooltip == null || tooltip.isEmpty() || tooltip.equals(key + "_tooltip")) {
      tooltip = null;
    }

    text = insertIcons(component, text);

    // Set the text for the component
    if (component instanceof JButton) {
      // Set the text for the button
      ((JButton) component).setText(text);
      if (tooltip != null && !tooltip.isEmpty()) {
        // Set the tooltip text for the component
        ((JButton) component).setToolTipText(tooltip);
      }
    } else if (component instanceof JToggleButton) {
      // Set the text for the toggle button
      ((JToggleButton) component).setText(text);
      if (tooltip != null && !tooltip.isEmpty()) {
        // Set the tooltip text for the component
        ((JToggleButton) component).setToolTipText(tooltip);
      }
    } else if (component instanceof JLabel) {
      // Set the text for the toggle button
      ((JLabel) component).setText(text);
      if (tooltip != null && !tooltip.isEmpty())
      {
        // Set the tooltip text for the component
        ((JLabel) component).setToolTipText(tooltip);
      }
    } else {
      // Handle other components like JLabel, JTextArea, etc.
      String componentType = component.getClass().getName();
      FRLogger.warn("The component type '" + componentType + "' is not supported");
    }


    // Handle other components like JLabel, JTextArea, etc.
  }
}
