import org.bytedeco.javacpp.*;
import static org.bytedeco.javacpp.lept.*;
import static org.bytedeco.javacpp.tesseract.*;

import org.apache.commons.lang3.ArrayUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.FileReader;
import java.util.*;

public class Main {
	private static final ArrayList<String> VALID_DICTIONARY = new ArrayList<>(); //arraylist of all words in dictionary.
	private static ArrayList<Character> allLetters = new ArrayList<>(); //arraylist of all chars
	private static Character centerLetter = null; //center character as local variable.
	
	private static String imagePath; //image path, saved as local variable.
	
	public static void main(String[] args) throws IOException { //main method
		if (manualOrOCR()) { //if returns false, go to manual mode, if true, go to automatic mode, using ocr.
			Image image = new Image(imagePath);
			allLetters = image.getAllLetters();
			centerLetter = image.getCenterLetter();
		} else {
			getCenterChar();
			getLetters();
		}
		
		System.out.println("\nLetters chosen: " + allLetters + "\n"); //confirm by showing all letter inputs.
		
		extractDictionary();
		
		System.out.println("Valid words: [" + VALID_DICTIONARY.size() + "]"); //number of valid word
		
		if (VALID_DICTIONARY.size() == 0) System.out.println("No matching words found!"); //if no words found, return 'no matching words.'
		for (int i = 0; i < VALID_DICTIONARY.size(); i++) { //extract all matching words from arraylist.
			System.out.println((i + 1) + " : " + VALID_DICTIONARY.get(i));
		}
	}

	private static boolean manualOrOCR() { //determine whether to get results manually or automatically. Also checks user inputs.
		
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter screenshot file path ('na' for manual mode): "); //get image file path,
		
		imagePath = scanner.next();
		
		if (imagePath.equals("na")) { //if input is 'na' return false, and go to manual input mode.
			System.out.println("\nMANUAL MODE");
			return false;
		}
		
		try {
			ImageIO.read(new File(imagePath)); //if unable to read file, raise exception, which is caught
			
			if (getFileExtension().equals("png") || getFileExtension().equals("jpg") || getFileExtension().equals("tif")) { //check if file extension is png, jpg or tif. If not, raise error.
				System.out.println("\nOCR MODE (this may take a few moments)"); //if extension matches, go to ocr mode.
				return true;
			} else {
				System.err.print("Invalid file type. Use [.png] [.jpg] [.tif]. "); //print error.
			}
		} catch(IOException e) {
			System.err.print("Invalid file path. ");
		}
		
		System.out.println("\nPlease enter letters manually.\n\nMANUAL MODE"); //if either file path not found, or extension false, got to manual mode.
		return false;
	}
	
	private static String getFileExtension() { //get the file extension from file path inputted by user.
		//COPIED FROM: https://stackoverflow.com/questions/25298691/how-to-check-the-file-type-in-java/25298748
		String fileName = new File(imagePath).getName();
		int dotIndex = fileName.lastIndexOf('.');
		return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
	}
	
	private static void getCenterChar() { //get center letter, user input.
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter center letter: ");
		centerLetter = scanner.next().toLowerCase(Locale.ROOT).charAt(0); //lower case letter, and get first char in sequence.
	}
	
	private static void getLetters() { //get outer letters, user input.
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter outer letters (no breaks or commas): "); //get 6 outer letters in string.
		
		String letters = scanner.next(); //scanner reads line.
		
		for (char c : uniqueCharacters(letters)) allLetters.add(c); //add every char to arraylist.
		
		allLetters.add(centerLetter);
	}
	
	private static char[] uniqueCharacters(String str) { //check if user char input has all unique characters. if not, remove duplicates.
		if (str.contains(centerLetter.toString())) //if contains double center letter, remove one of them.
			str = str.replace(centerLetter.toString(), "");
			
		char[] charArray = str.toCharArray();
		Arrays.sort(charArray);
			for (int i = 0; i < charArray.length - 1; i++) {
				if (charArray[i] == charArray[i + 1]) {
					charArray = ArrayUtils.remove(charArray, i);
				}
			}
			return charArray;
	}
	
	private static void extractDictionary() throws IOException { //extract all words from dictionary txt file.
		//RESOURCE: dictionary path found here: https://en.wikipedia.org/wiki/Words_(Unix)
		File dictionary = new File("/usr/share/dict/web2"); //unix standard dictionary file. This filepath is constant.
		BufferedReader readDictionary = new BufferedReader(new FileReader(dictionary)); //read file
		
		String word;
		
		while ((word = readDictionary.readLine()) != null) {
			if (isValidWord(word)) VALID_DICTIONARY.add(word); //archive every line in txt file to arraylist.
		}
	}
	
	private static boolean isValidWord(String word) {
		int minWordLength = 3; //word length must be greater than 3.
		if (word.length() <= minWordLength) return false;
		if (!word.contains(centerLetter.toString())) return false; //if word doesn't contain center letter, return false, and continue.
		
		int lettersMatch = 0;
		
		for (int i = 0; i < word.length(); i++) { //loop, cycles through all 7 characters for each letter in word. If they match lettersMatch++.
			for (Character charLetter : allLetters)
				if (charLetter == word.charAt(i)) lettersMatch++;
		}
		return lettersMatch == word.length(); //if the number of letters that match in word (to 7 available) equals to word length, then word must be valid.
	}
}

class Image { //get images, modify images to use on ocr.
	private final String imagePath;
	
	private BufferedImage image;
	private int width;
	private int height;
	
	private BufferedImage tiffImage;
	private int tiffWidth;
	private int tiffHeight;
	private int h;
	
	private final static ArrayList<Character> allLetters = new ArrayList<>(); //char arraylist of all letters
	private static Character centerLetter = null; //center char.
	
	public Image(String path) throws IOException { //constructor
		imagePath = path;
		
		getImage();
		grayScale();
		replacePixel();
		crop();
		
		Tesseract tesseract_c_TOP = new Tesseract(c_TOP());
		allLetters.add(tesseract_c_TOP.getCharacter()); //retrieve character using tesseract and single-character images. (same for others bellow)
		
		Tesseract tesseract_c_BOT = new Tesseract(c_BOT());
		allLetters.add(tesseract_c_BOT.getCharacter());
		
		Tesseract tesseract_l_TOP = new Tesseract(l_TOP());
		allLetters.add(tesseract_l_TOP.getCharacter());
		
		Tesseract tesseract_l_BOT = new Tesseract(l_BOT());
		allLetters.add(tesseract_l_BOT.getCharacter());
		
		Tesseract tesseract_r_TOP = new Tesseract(r_TOP());
		allLetters.add(tesseract_r_TOP.getCharacter());
		
		Tesseract tesseract_r_BOT = new Tesseract(r_BOT());
		allLetters.add(tesseract_r_BOT.getCharacter());
		
		Tesseract tesseract_c_MID = new Tesseract(c_MID()); //middle character, add to char arraylist, and set to CenterLetter var.
		allLetters.add(tesseract_c_MID.getCharacter());
		centerLetter = tesseract_c_MID.getCharacter();
	}
	
	private void getImage() throws IOException { //retrieve the image using file path. Get variables e.g. width & height & BufferedImage
		File input = new File(imagePath);
		image = ImageIO.read(input);
		width = image.getWidth();
		height = image.getHeight();
	}
	
	private void grayScale() {
		//Make image grayscale to make next method possible. NOT MY CODE! Copied from here: https://stackoverflow.com/a/4818980
		try {
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					Color c = new Color(image.getRGB(j, i));
					
					int red = (int)(c.getRed() * 0.299), green = (int)(c.getGreen() * 0.587), blue = (int)(c.getBlue() * 0.114);
					Color newColor = new Color(red + green + blue, red + green + blue,red + green + blue);
					image.setRGB(j, i, newColor.getRGB());
				}
			}
			File output = new File("src/main/resources/mod_images/grayscale.tif");
			ImageIO.write(image, "tiff", output);
				
		} catch (Exception ignored) {}
	}
	
	private void replacePixel() throws IOException { //make binary color image, solely using black or white pixel.
		int[] pixels = new int[width * height]; //pixel array
		image.getRGB(0, 0, width, height, pixels, 0, width); //set hex color values to pixel array
		
		for (int i = 0; i < pixels.length; i++) { //if pixel color is any one of these shades of grey, make them black.
			if (pixels[i] == 0xFF010101 || pixels[i] == 0xFF020202 || pixels[i] == 0xFF030303 || pixels[i] == 0xFF040404 || pixels[i] == 0xFF050505 || pixels[i] == 0xFF060606 || pixels[i] == 0xFF070707 || pixels[i] == 0xFF080808 || pixels[i] == 0xFF090909 || pixels[i] == 0xFF0a0a0a || pixels[i] == 0xFF0b0b0b || pixels[i] == 0xFF0c0c0c || pixels[i] == 0xFF0d0d0d) {
				pixels[i] = 0xFF000000;
			}
			
			if (pixels[i] != 0xFF000000) //if any other pixels aren't black, make white.
				pixels[i] = 0xFFffffff;
		}
		
		image.setRGB(0, 0, width, height, pixels, 0, width); //replace image will new colors.
		File output = new File("src/main/resources/mod_images/final.tif"); //output new file.
		ImageIO.write(image, "tiff", output);
	}
	
	private void crop() throws IOException { //crops full photo to known proportions.
		/*
		* Entirely my idea, :
		* Program will scan image by column or row, and once it encounters black hexadecimal value,
		* will save column/row index and crop at that location.
		* This was procedure was done on all sides, and a 5 pixel white border was left on image frame.
		*
		* The purpose of this is to create a pic of known proportions, so that letters can be accurately located*/
		
		
		int[] pixels = new int[width * height]; //create array of all pixels in image.
		int x = 0, y = 0, w = width, h = height;
		
		image.getRGB(0, 0, width, height, pixels, 0, width); //set each indices to pixel hex value.
		
		outerLoop: //loop cycles through image by column from left to right. (create left crop)
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (pixels[(j * width) + i] == 0xFF000000) { //if black pixel, exit loop and save index.
					x = (i - 5);
					break outerLoop;
				}
			}
		}
		
		outerLoop: //loop cycles through image by column from right to left. (create right crop.)
		for (int i = (width - 1); i >= 0; i--) {
			for (int j = 0; j < height; j++) {
				if (pixels[(j * width) + i] == 0xFF000000) {
					w = (i + 5 - x);
					break outerLoop;
				}
			}
		}
		
		outerLoop: //loop cycles through image by column from top to bottom. (create top crop.)
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (pixels[(i * width) + j] == 0xFF000000) {
					y = (i - 5);
					break outerLoop;
				}
			}
		}
		
		outerLoop: //loop cycles through image by column from bottom to top. (create bottom crop.)
		for (int i = (height - 1); i >= 0; i--) {
			for (int j = 0; j < width; j++) {
				if (pixels[(i * width) + j] == 0xFF000000) {
					h = (i + 5 - y);
					break outerLoop;
				}
			}
		}
		
		Rectangle rect = new Rectangle(x, y, w, h); //set rectangle dimension + coordinate of sub-image.
		//RESOURCE: https://stackoverflow.com/a/4818980
		
		tiffWidth = w;
		tiffHeight = h;
		tiffImage = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
		
		ImageIO.write(tiffImage, "tiff", new File("src/main/resources/mod_images/cropped.tif")); //file location.
	}
	
	private String c_TOP() throws IOException { //center-top character. (code essentially duplicated for each letter)
		h = (int) (tiffHeight * 0.143);
		int x = ((tiffWidth / 2) - (h / 2)), y = 0;
		
		Rectangle rect = new Rectangle(x, y, h, h); // x-y coordinate based on location of letter, relation to pic width & height. Dimension of rect is h * h.
		
		String pathName = "src/main/resources/letters/c_TOP.tif"; //where to store sub-section of image.
		
		BufferedImage dest = tiffImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
		ImageIO.write(dest, "tiff", new File(pathName));
		
		return pathName; //return the pathname to be references elsewhere, just to ensure same paths are used if ever changed.
	}
	
	private String c_BOT() throws IOException { //center-bottom char.
		int h = (int) (tiffHeight * 0.143), x = ((tiffWidth / 2) - (h / 2)), y = (tiffHeight - h);
		
		Rectangle rect = new Rectangle(x, y, h, h);
		
		String pathName = "src/main/resources/letters/c_BOT.tif";
		
		BufferedImage dest = tiffImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
		ImageIO.write(dest, "tiff", new File(pathName));
		
		return pathName;
	}
	
	private String l_TOP() throws IOException { //left-top char.
		int x = 0, y = (int) (tiffHeight * 0.214);
		
		Rectangle rect = new Rectangle(x, y, h, h);
		
		String pathName = "src/main/resources/letters/l_TOP.tif";
		
		BufferedImage dest = tiffImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
		ImageIO.write(dest, "tiff", new File(pathName));
		
		return pathName;
	}
	
	private String l_BOT() throws IOException { //left-bottom
		int x = 0, y = (int) (tiffHeight * 0.657);
		
		Rectangle rect = new Rectangle(x, y, h, h);
		
		String pathName = "src/main/resources/letters/l_BOT.tif";
		
		BufferedImage dest = tiffImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
		ImageIO.write(dest, "tiff", new File(pathName));
		
		return pathName;
	}
	
	private String r_TOP() throws IOException { //right-top character
		int x = (tiffWidth - h), y = (int) (tiffHeight * 0.214);
		
		Rectangle rect = new Rectangle(x, y, h, h);
		
		String pathName = "src/main/resources/letters/r_TOP.tif";
		
		BufferedImage dest = tiffImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
		ImageIO.write(dest, "tiff", new File(pathName));
		
		return pathName;
	}
	
	private String r_BOT() throws IOException { //right-bottom character.
		int x = (tiffWidth - h), y = (int) (tiffHeight * 0.657);
		
		Rectangle rect = new Rectangle(x, y, h, h);
		
		String pathName = "src/main/resources/letters/r_BOT.tif";
		
		BufferedImage dest = tiffImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
		ImageIO.write(dest, "tiff", new File(pathName));
		
		return pathName;
	}
	
	private String c_MID() throws IOException { //center-middle character.
		int x = ((tiffWidth / 2) - (h / 2)), y = ((tiffHeight / 2) - (h / 2));
		
		Rectangle rect = new Rectangle(x, y, h, h);
		
		String pathName = "src/main/resources/letters/c_MID.tif";
		
		BufferedImage dest = tiffImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
		ImageIO.write(dest, "tiff", new File(pathName));
		
		return pathName;
	}
	
	public ArrayList<Character> getAllLetters() { //return arraylist char
		return allLetters; //getter
	}
	
	public Character getCenterLetter() { //return char centerLetter.
		return centerLetter; //getter
	}
}

class Tesseract { //tesseract ocr class. jar files imported from maven.
	private static String imagePath;
	private static Character character = '0';
	
	public Tesseract(String path) { //constructor
		imagePath = path; //setter
		tesseractOCR();
	}
	
	private static void tesseractOCR() {
		/* RESOURCES USED:
		https://stackoverflow.com/questions/20285818/how-to-hide-or-ignore-system-err-outputs-in-intellij-console
		https://stackoverflow.com/questions/3163399/difference-between-system-out-println-and-system-err-println
		https://github.com/piersy/BasicTesseractExample/blob/master/src/test/java/BasicTesseractExampleTest.java
		https://github.com/piersy/BasicTesseractExample/blob/master/pom.xml
		https://github.com/bytedeco/javacpp-presets/issues/654#issuecomment-443901588*/
		
		
		System.err.close(); //Remove dpi errors thrown by Tesseract (clutters console).
		
		BytePointer outText;
		
		TessBaseAPI api = new TessBaseAPI();
		
		// Initialize tesseract-ocr with English, without specifying tessdata path
		if (api.Init("tessdata", "ENG") != 0) {
			System.err.println("Could not initialize tesseract.");
			System.exit(1);
		}
		
		PIX image = pixRead(imagePath);
		
		// Open input image with leptonica library
		api.SetImage(image);
		// Get OCR result
		outText = api.GetUTF8Text();
		String string = outText.getString();
		
		// Destroy used object and release memory
		api.End();
		outText.deallocate();
		pixDestroy(image);
		
		try {
			if (string.contains("0")) string = string.replace('0', 'o'); //ocr commonly mistakes o or 0. Make replacement automatically.
			character = string.toLowerCase(Locale.ROOT).charAt(0);
		} catch (StringIndexOutOfBoundsException e) {
			character = 'i'; // if 'I' not detected, raises exception invoked by charAt(). If exception raised, set character = i.
		}
	}
	
	public Character getCharacter() { //get character produced from ocr.
		return character; //getter
	}
}