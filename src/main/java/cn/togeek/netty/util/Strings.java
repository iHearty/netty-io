package cn.togeek.netty.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class Strings {
   public static final String[] EMPTY_ARRAY = new String[0];

   private static final RandomBasedUUIDGenerator RANDOM_UUID_GENERATOR =
      new RandomBasedUUIDGenerator();

   private static final UUIDGenerator TIME_UUID_GENERATOR =
      new TimeBasedUUIDGenerator();

   /**
    * Check that the given CharSequence is neither <code>null</code> nor of
    * length 0. Note: Will return <code>true</code> for a CharSequence that
    * purely consists of whitespace.
    * 
    * <pre>
    * StringUtils.hasLength(null) = false
    * StringUtils.hasLength("") = false
    * StringUtils.hasLength(" ") = true
    * StringUtils.hasLength("Hello") = true
    * </pre>
    *
    * @param str the CharSequence to check (may be <code>null</code>)
    * @return <code>true</code> if the CharSequence is not null and has length
    * @see #hasText(String)
    */
   public static boolean hasLength(CharSequence str) {
      return (str != null && str.length() > 0);
   }

   /**
    * Check that the given String is neither <code>null</code> nor of length 0.
    * Note: Will return <code>true</code> for a String that purely consists of
    * whitespace.
    *
    * @param str the String to check (may be <code>null</code>)
    * @return <code>true</code> if the String is not null and has length
    * @see #hasLength(CharSequence)
    */
   public static boolean hasLength(String str) {
      return hasLength((CharSequence) str);
   }

   /**
    * Check that the given CharSequence is either <code>null</code> or of length
    * 0. Note: Will return <code>false</code> for a CharSequence that purely
    * consists of whitespace.
    * 
    * <pre>
    * StringUtils.isEmpty(null) = true
    * StringUtils.isEmpty("") = true
    * StringUtils.isEmpty(" ") = false
    * StringUtils.isEmpty("Hello") = false
    * </pre>
    *
    * @param str the CharSequence to check (may be <code>null</code>)
    * @return <code>true</code> if the CharSequence is either null or has a zero
    *         length
    */
   public static boolean isEmpty(CharSequence str) {
      return !hasLength(str);
   }

   /**
    * Check whether the given CharSequence has actual text. More specifically,
    * returns <code>true</code> if the string not <code>null</code>, its length
    * is greater than 0, and it contains at least one non-whitespace character.
    * 
    * <pre>
    * StringUtils.hasText(null) = false
    * StringUtils.hasText("") = false
    * StringUtils.hasText(" ") = false
    * StringUtils.hasText("12345") = true
    * StringUtils.hasText(" 12345 ") = true
    * </pre>
    *
    * @param str the CharSequence to check (may be <code>null</code>)
    * @return <code>true</code> if the CharSequence is not <code>null</code>,
    *         its length is greater than 0, and it does not contain whitespace
    *         only
    * @see java.lang.Character#isWhitespace
    */
   public static boolean hasText(CharSequence str) {
      if(!hasLength(str)) {
         return false;
      }

      int strLen = str.length();

      for(int i = 0; i < strLen; i++) {
         if(!Character.isWhitespace(str.charAt(i))) {
            return true;
         }
      }

      return false;
   }

   /**
    * Check whether the given String has actual text. More specifically, returns
    * <code>true</code> if the string not <code>null</code>, its length is
    * greater than 0, and it contains at least one non-whitespace character.
    *
    * @param str the String to check (may be <code>null</code>)
    * @return <code>true</code> if the String is not <code>null</code>, its
    *         length is greater than 0, and it does not contain whitespace only
    * @see #hasText(CharSequence)
    */
   public static boolean hasText(String str) {
      return hasText((CharSequence) str);
   }

   /**
    * Check whether the given CharSequence contains any whitespace characters.
    *
    * @param str the CharSequence to check (may be <code>null</code>)
    * @return <code>true</code> if the CharSequence is not empty and contains at
    *         least 1 whitespace character
    * @see java.lang.Character#isWhitespace
    */
   public static boolean containsWhitespace(CharSequence str) {
      if(!hasLength(str)) {
         return false;
      }

      int strLen = str.length();

      for(int i = 0; i < strLen; i++) {
         if(Character.isWhitespace(str.charAt(i))) {
            return true;
         }
      }

      return false;
   }

   /**
    * Trim leading whitespace from the given String.
    *
    * @param str the String to check
    * @return the trimmed String
    * @see java.lang.Character#isWhitespace
    */
   public static String trimLeadingWhitespace(String str) {
      if(!hasLength(str)) {
         return str;
      }

      StringBuilder sb = new StringBuilder(str);

      while(sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
         sb.deleteCharAt(0);
      }

      return sb.toString();
   }

   /**
    * Trim all occurrences of the supplied leading character from the given
    * String.
    *
    * @param str the String to check
    * @param leadingCharacter the leading character to be trimmed
    * @return the trimmed String
    */
   public static String trimLeadingCharacter(String str,
                                             char leadingCharacter)
   {
      if(!hasLength(str)) {
         return str;
      }

      StringBuilder sb = new StringBuilder(str);

      while(sb.length() > 0 && sb.charAt(0) == leadingCharacter) {
         sb.deleteCharAt(0);
      }

      return sb.toString();
   }

   /**
    * Test whether the given string matches the given substring at the given
    * index.
    *
    * @param str the original string (or StringBuilder)
    * @param index the index in the original string to start matching against
    * @param substring the substring to match at the given index
    */
   public static boolean substringMatch(CharSequence str,
                                        int index,
                                        CharSequence substring)
   {
      for(int j = 0; j < substring.length(); j++) {
         int i = index + j;

         if(i >= str.length() || str.charAt(i) != substring.charAt(j)) {
            return false;
         }
      }

      return true;
   }

   /**
    * Count the occurrences of the substring in string s.
    *
    * @param str string to search in. Return 0 if this is null.
    * @param sub string to search for. Return 0 if this is null.
    */
   public static int countOccurrencesOf(String str, String sub) {
      if(str == null || sub == null || str.length() == 0 || sub.length() == 0) {
         return 0;
      }

      int count = 0;
      int pos = 0;
      int idx;

      while((idx = str.indexOf(sub, pos)) != -1) {
         ++count;
         pos = idx + sub.length();
      }

      return count;
   }

   /**
    * Replace all occurrences of a substring within a string with another
    * string.
    *
    * @param inString String to examine
    * @param oldPattern String to replace
    * @param newPattern String to insert
    * @return a String with the replacements
    */
   public static String replace(String inString,
                                String oldPattern,
                                String newPattern)
   {
      if(!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
         return inString;
      }

      StringBuilder sb = new StringBuilder();
      // our position in the old string
      int pos = 0;
      int index = inString.indexOf(oldPattern);
      // the index of an occurrence we've found, or -1
      int patLen = oldPattern.length();

      while(index >= 0) {
         sb.append(inString.substring(pos, index));
         sb.append(newPattern);
         pos = index + patLen;
         index = inString.indexOf(oldPattern, pos);
      }

      sb.append(inString.substring(pos));
      // remember to append any characters to the right of a match
      return sb.toString();
   }

   /**
    * Delete all occurrences of the given substring.
    *
    * @param inString the original String
    * @param pattern the pattern to delete all occurrences of
    * @return the resulting String
    */
   public static String delete(String inString, String pattern) {
      return replace(inString, pattern, "");
   }

   /**
    * Delete any character in a given String.
    *
    * @param inString the original String
    * @param charsToDelete a set of characters to delete. E.g. "az\n" will
    *           delete 'a's, 'z's and new lines.
    * @return the resulting String
    */
   public static String deleteAny(String inString, String charsToDelete) {
      if(!hasLength(inString) || !hasLength(charsToDelete)) {
         return inString;
      }

      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < inString.length(); i++) {
         char c = inString.charAt(i);
         if(charsToDelete.indexOf(c) == -1) {
            sb.append(c);
         }
      }

      return sb.toString();
   }

   // ---------------------------------------------------------------------
   // Convenience methods for working with formatted Strings
   // ---------------------------------------------------------------------

   /**
    * Quote the given String with single quotes.
    *
    * @param str the input String (e.g. "myString")
    * @return the quoted String (e.g. "'myString'"), or <code>null</code> if the
    *         input was <code>null</code>
    */
   public static String quote(String str) {
      return (str != null ? "'" + str + "'" : null);
   }

   /**
    * Unqualify a string qualified by a separator character. For example,
    * "this:name:is:qualified" returns "qualified" if using a ':' separator.
    *
    * @param qualifiedName the qualified name
    * @param separator the separator
    */
   public static String unqualify(String qualifiedName, char separator) {
      return qualifiedName.substring(qualifiedName.lastIndexOf(separator) + 1);
   }

   /**
    * Capitalize a <code>String</code>, changing the first letter to upper case
    * as per {@link Character#toUpperCase(char)}. No other letters are changed.
    *
    * @param str the String to capitalize, may be <code>null</code>
    * @return the capitalized String, <code>null</code> if null
    */
   public static String capitalize(String str) {
      return changeFirstCharacterCase(str, true);
   }

   private static String changeFirstCharacterCase(String str,
                                                  boolean capitalize)
   {
      if(str == null || str.length() == 0) {
         return str;
      }

      StringBuilder sb = new StringBuilder(str.length());

      if(capitalize) {
         sb.append(Character.toUpperCase(str.charAt(0)));
      }
      else {
         sb.append(Character.toLowerCase(str.charAt(0)));
      }

      sb.append(str.substring(1));
      return sb.toString();
   }

   /**
    * Copy the given Collection into a String array. The Collection must contain
    * String elements only.
    *
    * @param collection the Collection to copy
    * @return the String array (<code>null</code> if the passed-in Collection
    *         was <code>null</code>)
    */
   public static String[] toStringArray(Collection<String> collection) {
      if(collection == null) {
         return null;
      }

      return collection.toArray(new String[collection.size()]);
   }

   public static Set<String> splitStringByCommaToSet(final String s) {
      return splitStringToSet(s, ',');
   }

   public static String[] splitStringByCommaToArray(final String s) {
      return splitStringToArray(s, ',');
   }

   public static Set<String> splitStringToSet(final String s, final char c) {
      final char[] chars = s.toCharArray();
      int count = 1;

      for(final char x : chars) {
         if(x == c) {
            count++;
         }
      }

      final Set<String> result = new HashSet<>(count);
      final int len = chars.length;
      int start = 0; // starting index in chars of the current substring.
      int pos = 0; // current index in chars.

      for(; pos < len; pos++) {
         if(chars[pos] == c) {
            int size = pos - start;

            if(size > 0) { // only add non empty strings
               result.add(new String(chars, start, size));
            }

            start = pos + 1;
         }
      }

      int size = pos - start;

      if(size > 0) {
         result.add(new String(chars, start, size));
      }

      return result;
   }

   public static String[] splitStringToArray(final CharSequence s,
                                             final char c)
   {
      if(s == null || s.length() == 0) {
         return Strings.EMPTY_ARRAY;
      }

      int count = 1;

      for(int i = 0; i < s.length(); i++) {
         if(s.charAt(i) == c) {
            count++;
         }
      }

      final String[] result = new String[count];
      final StringBuilder builder = new StringBuilder();
      int res = 0;

      for(int i = 0; i < s.length(); i++) {
         if(s.charAt(i) == c) {
            if(builder.length() > 0) {
               result[res++] = builder.toString();
               builder.setLength(0);
            }

         }
         else {
            builder.append(s.charAt(i));
         }
      }

      if(builder.length() > 0) {
         result[res++] = builder.toString();
      }

      if(res != count) {
         // we have empty strings, copy over to a new array
         String[] result1 = new String[res];
         System.arraycopy(result, 0, result1, 0, res);
         return result1;
      }

      return result;
   }

   /**
    * Split a String at the first occurrence of the delimiter. Does not include
    * the delimiter in the result.
    *
    * @param toSplit the string to split
    * @param delimiter to split the string up with
    * @return a two element array with index 0 being before the delimiter, and
    *         index 1 being after the delimiter (neither element includes the
    *         delimiter); or <code>null</code> if the delimiter wasn't found in
    *         the given input String
    */
   public static String[] split(String toSplit, String delimiter) {
      if(!hasLength(toSplit) || !hasLength(delimiter)) {
         return null;
      }

      int offset = toSplit.indexOf(delimiter);

      if(offset < 0) {
         return null;
      }

      String beforeDelimiter = toSplit.substring(0, offset);
      String afterDelimiter = toSplit.substring(offset + delimiter.length());
      return new String[] {beforeDelimiter, afterDelimiter};
   }

   /**
    * Take an array Strings and split each element based on the given delimiter.
    * A <code>Properties</code> instance is then generated, with the left of the
    * delimiter providing the key, and the right of the delimiter providing the
    * value.
    * <p>
    * Will trim both the key and value before adding them to the
    * <code>Properties</code> instance.
    *
    * @param array the array to process
    * @param delimiter to split each element using (typically the equals symbol)
    * @param charsToDelete one or more characters to remove from each element
    *           prior to attempting the split operation (typically the quotation
    *           mark symbol), or <code>null</code> if no removal should occur
    * @return a <code>Properties</code> instance representing the array
    *         contents, or <code>null</code> if the array to process was
    *         <code>null</code> or empty
    */
   public static Properties splitArrayElementsIntoProperties(String[] array,
                                                             String delimiter,
                                                             String charsToDelete)
   {
      if(isEmpty(array)) {
         return null;
      }

      Properties result = new Properties();

      for(String element : array) {
         if(charsToDelete != null) {
            element = deleteAny(element, charsToDelete);
         }

         String[] splittedElement = split(element, delimiter);

         if(splittedElement == null) {
            continue;
         }

         result.setProperty(splittedElement[0].trim(),
            splittedElement[1].trim());
      }

      return result;
   }

   /**
    * Tokenize the given String into a String array via a StringTokenizer. Trims
    * tokens and omits empty tokens.
    * <p>
    * The given delimiters string is supposed to consist of any number of
    * delimiter characters. Each of those characters can be used to separate
    * tokens. A delimiter is always a single character; for multi-character
    * delimiters, consider using <code>delimitedListToStringArray</code>
    *
    * @param str the String to tokenize
    * @param delimiters the delimiter characters, assembled as String (each of
    *           those characters is individually considered as delimiter).
    * @return an array of the tokens
    * @see java.util.StringTokenizer
    * @see java.lang.String#trim()
    * @see #delimitedListToStringArray
    */
   public static String[] tokenizeToStringArray(String str, String delimiters) {
      return tokenizeToStringArray(str, delimiters, true, true);
   }

   /**
    * Tokenize the given String into a String array via a StringTokenizer.
    * <p>
    * The given delimiters string is supposed to consist of any number of
    * delimiter characters. Each of those characters can be used to separate
    * tokens. A delimiter is always a single character; for multi-character
    * delimiters, consider using <code>delimitedListToStringArray</code>
    *
    * @param str the String to tokenize
    * @param delimiters the delimiter characters, assembled as String (each of
    *           those characters is individually considered as delimiter)
    * @param trimTokens trim the tokens via String's <code>trim</code>
    * @param ignoreEmptyTokens omit empty tokens from the result array (only
    *           applies to tokens that are empty after trimming; StringTokenizer
    *           will not consider subsequent delimiters as token in the first
    *           place).
    * @return an array of the tokens (<code>null</code> if the input String was
    *         <code>null</code>)
    * @see java.util.StringTokenizer
    * @see java.lang.String#trim()
    * @see #delimitedListToStringArray
    */
   public static String[] tokenizeToStringArray(String str,
                                                String delimiters,
                                                boolean trimTokens,
                                                boolean ignoreEmptyTokens)
   {
      if(str == null) {
         return null;
      }

      StringTokenizer st = new StringTokenizer(str, delimiters);
      List<String> tokens = new ArrayList<>();

      while(st.hasMoreTokens()) {
         String token = st.nextToken();

         if(trimTokens) {
            token = token.trim();
         }

         if(!ignoreEmptyTokens || token.length() > 0) {
            tokens.add(token);
         }
      }

      return toStringArray(tokens);
   }

   /**
    * Take a String which is a delimited list and convert it to a String array.
    * <p>
    * A single delimiter can consists of more than one character: It will still
    * be considered as single delimiter string, rather than as bunch of
    * potential delimiter characters - in contrast to
    * <code>tokenizeToStringArray</code>.
    *
    * @param str the input String
    * @param delimiter the delimiter between elements (this is a single
    *           delimiter, rather than a bunch individual delimiter characters)
    * @return an array of the tokens in the list
    * @see #tokenizeToStringArray
    */
   public static String[] delimitedListToStringArray(String str,
                                                     String delimiter)
   {
      return delimitedListToStringArray(str, delimiter, null);
   }

   /**
    * Take a String which is a delimited list and convert it to a String array.
    * <p>
    * A single delimiter can consists of more than one character: It will still
    * be considered as single delimiter string, rather than as bunch of
    * potential delimiter characters - in contrast to
    * <code>tokenizeToStringArray</code>.
    *
    * @param str the input String
    * @param delimiter the delimiter between elements (this is a single
    *           delimiter, rather than a bunch individual delimiter characters)
    * @param charsToDelete a set of characters to delete. Useful for deleting
    *           unwanted line breaks: e.g. "\r\n\f" will delete all new lines
    *           and line feeds in a String.
    * @return an array of the tokens in the list
    * @see #tokenizeToStringArray
    */
   public static String[] delimitedListToStringArray(String str,
                                                     String delimiter,
                                                     String charsToDelete)
   {
      if(str == null) {
         return new String[0];
      }

      if(delimiter == null) {
         return new String[] {str};
      }

      List<String> result = new ArrayList<>();

      if("".equals(delimiter)) {
         for(int i = 0; i < str.length(); i++) {
            result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
         }
      }
      else {
         int pos = 0;
         int delPos;

         while((delPos = str.indexOf(delimiter, pos)) != -1) {
            result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
            pos = delPos + delimiter.length();
         }

         if(str.length() > 0 && pos <= str.length()) {
            // Add rest of String, but not in case of empty input.
            result.add(deleteAny(str.substring(pos), charsToDelete));
         }
      }

      return toStringArray(result);
   }

   /**
    * Convert a CSV list into an array of Strings.
    *
    * @param str the input String
    * @return an array of Strings, or the empty array in case of empty input
    */
   public static String[] commaDelimitedListToStringArray(String str) {
      return delimitedListToStringArray(str, ",");
   }

   /**
    * Convenience method to convert a CSV string list to a set. Note that this
    * will suppress duplicates.
    *
    * @param str the input String
    * @return a Set of String entries in the list
    */
   public static Set<String> commaDelimitedListToSet(String str) {
      Set<String> set = new TreeSet<>();
      String[] tokens = commaDelimitedListToStringArray(str);
      set.addAll(Arrays.asList(tokens));
      return set;
   }

   /**
    * Convenience method to return a Collection as a delimited (e.g. CSV)
    * String. E.g. useful for <code>toString()</code> implementations.
    *
    * @param coll the Collection to display
    * @param delim the delimiter to use (probably a ",")
    * @param prefix the String to start each element with
    * @param suffix the String to end each element with
    * @return the delimited String
    */
   public static String collectionToDelimitedString(Iterable<?> coll,
                                                    String delim,
                                                    String prefix,
                                                    String suffix)
   {
      return collectionToDelimitedString(coll, delim, prefix, suffix,
         new StringBuilder());
   }

   public static String collectionToDelimitedString(Iterable<?> coll,
                                                    String delim,
                                                    String prefix,
                                                    String suffix,
                                                    StringBuilder sb)
   {
      Iterator<?> it = coll.iterator();

      while(it.hasNext()) {
         sb.append(prefix).append(it.next()).append(suffix);

         if(it.hasNext()) {
            sb.append(delim);
         }
      }

      return sb.toString();
   }

   /**
    * Convenience method to return a Collection as a delimited (e.g. CSV)
    * String. E.g. useful for <code>toString()</code> implementations.
    *
    * @param coll the Collection to display
    * @param delim the delimiter to use (probably a ",")
    * @return the delimited String
    */
   public static String collectionToDelimitedString(Iterable<?> coll,
                                                    String delim)
   {
      return collectionToDelimitedString(coll, delim, "", "");
   }

   /**
    * Convenience method to return a Collection as a CSV String. E.g. useful for
    * <code>toString()</code> implementations.
    *
    * @param coll the Collection to display
    * @return the delimited String
    */
   public static String collectionToCommaDelimitedString(Iterable<?> coll) {
      return collectionToDelimitedString(coll, ",");
   }

   /**
    * Convenience method to return a String array as a delimited (e.g. CSV)
    * String. E.g. useful for <code>toString()</code> implementations.
    *
    * @param arr the array to display
    * @param delim the delimiter to use (probably a ",")
    * @return the delimited String
    */
   public static String arrayToDelimitedString(Object[] arr, String delim) {
      return arrayToDelimitedString(arr, delim, new StringBuilder());
   }

   public static String arrayToDelimitedString(Object[] arr,
                                               String delim,
                                               StringBuilder sb)
   {
      if(isEmpty(arr)) {
         return "";
      }

      for(int i = 0; i < arr.length; i++) {
         if(i > 0) {
            sb.append(delim);
         }

         sb.append(arr[i]);
      }

      return sb.toString();
   }

   /**
    * Convenience method to return a String array as a CSV String. E.g. useful
    * for <code>toString()</code> implementations.
    *
    * @param arr the array to display
    * @return the delimited String
    */
   public static String arrayToCommaDelimitedString(Object[] arr) {
      return arrayToDelimitedString(arr, ",");
   }

   /**
    * Format the double value with a single decimal points, trimming trailing
    * '.0'.
    */
   public static String format1Decimals(double value, String suffix) {
      String p = String.valueOf(value);
      int ix = p.indexOf('.') + 1;
      int ex = p.indexOf('E');
      char fraction = p.charAt(ix);

      if(fraction == '0') {
         if(ex != -1) {
            return p.substring(0, ix - 1) + p.substring(ex) + suffix;
         }
         else {
            return p.substring(0, ix - 1) + suffix;
         }
      }
      else {
         if(ex != -1) {
            return p.substring(0, ix) + fraction + p.substring(ex) + suffix;
         }
         else {
            return p.substring(0, ix) + fraction + suffix;
         }
      }
   }

   public static String toCamelCase(String value) {
      return toCamelCase(value, null);
   }

   public static String toCamelCase(String value, StringBuilder sb) {
      boolean changed = false;

      for(int i = 0; i < value.length(); i++) {
         char c = value.charAt(i);
         // e.g. _name stays as-is, _first_name becomes _firstName
         if(c == '_' && i > 0) {
            if(!changed) {
               if(sb != null) {
                  sb.setLength(0);
               }
               else {
                  sb = new StringBuilder();
               }

               // copy it over here
               for(int j = 0; j < i; j++) {
                  sb.append(value.charAt(j));
               }

               changed = true;
            }

            if(i < value.length() - 1) {
               sb.append(Character.toUpperCase(value.charAt(++i)));
            }
         }
         else {
            if(changed) {
               sb.append(c);
            }
         }
      }

      if(!changed) {
         return value;
      }

      return sb.toString();
   }

   public static String toUnderscoreCase(String value) {
      return toUnderscoreCase(value, null);
   }

   public static String toUnderscoreCase(String value, StringBuilder sb) {
      boolean changed = false;

      for(int i = 0; i < value.length(); i++) {
         char c = value.charAt(i);
         if(Character.isUpperCase(c)) {
            if(!changed) {
               if(sb != null) {
                  sb.setLength(0);
               }
               else {
                  sb = new StringBuilder();
               }

               // copy it over here
               for(int j = 0; j < i; j++) {
                  sb.append(value.charAt(j));
               }

               changed = true;

               if(i == 0) {
                  sb.append(Character.toLowerCase(c));
               }
               else {
                  sb.append('_');
                  sb.append(Character.toLowerCase(c));
               }
            }
            else {
               sb.append('_');
               sb.append(Character.toLowerCase(c));
            }
         }
         else {
            if(changed) {
               sb.append(c);
            }
         }
      }

      if(!changed) {
         return value;
      }

      return sb.toString();
   }

   /**
    * Determine whether the given array is empty: i.e. <code>null</code> or of
    * zero length.
    *
    * @param array the array to check
    */
   private static boolean isEmpty(Object[] array) {
      return (array == null || array.length == 0);
   }

   /**
    * Return substring(beginIndex, endIndex) that is impervious to string
    * length.
    */
   public static String substring(String s, int beginIndex, int endIndex) {
      if(s == null) {
         return s;
      }

      int realEndIndex = s.length() > 0 ? s.length() - 1 : 0;

      if(endIndex > realEndIndex) {
         return s.substring(beginIndex);
      }
      else {
         return s.substring(beginIndex, endIndex);
      }
   }

   /**
    * Returns a Base64 encoded version of a Version 4.0 compatible UUID as
    * defined here: http://www.ietf.org/rfc/rfc4122.txt, using a private
    * {@code SecureRandom} instance
    */
   public static String randomBase64UUID() {
      return RANDOM_UUID_GENERATOR.getBase64UUID();
   }

   /**
    * Returns a Base64 encoded version of a Version 4.0 compatible UUID as
    * defined here: http://www.ietf.org/rfc/rfc4122.txt, using the provided
    * {@code Random} instance
    */
   public static String randomBase64UUID(Random random) {
      return RANDOM_UUID_GENERATOR.getBase64UUID(random);
   }

   /**
    * Generates a time-based UUID (similar to Flake IDs), which is preferred
    * when generating an ID to be indexed into a Lucene index as primary key.
    * The id is opaque and the implementation is free to change at any time!
    */
   public static String base64UUID() {
      return TIME_UUID_GENERATOR.getBase64UUID();
   }

   /**
    * Truncates string to a length less than length. Backtracks to throw out
    * high surrogates.
    */
   public static String cleanTruncate(String s, int length) {
      if(s == null) {
         return s;
      }

      /*
       * Its pretty silly for you to truncate to 0 length but just in case
       * someone does this shouldn't break.
       */
      if(length == 0) {
         return "";
      }

      if(length >= s.length()) {
         return s;
      }

      if(Character.isHighSurrogate(s.charAt(length - 1))) {
         length--;
      }

      return s.substring(0, length);
   }

   public static String coalesceToEmpty(String s) {
      return s == null ? "" : s;
   }

   private Strings() {
   }
}