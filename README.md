# NYT_spelling_bee_solver

### [*New York Times Spelling Bee*](https://www.nytimes.com/puzzles/spelling-bee) solver.
***
### _PROJECT SUMMARY_:
In this Java program, we solve a NYTimes Spellling Bee by extracting all the possible English words that can be created from the given 7 letters. These characters can be copied into the program __manually__, by typing them into console, or __semi-automatically__, where you can take a screenshot of the game, and use the [Tesseract OCR](https://github.com/tesseract-ocr/tesseract) to extract the letters.
***
### _THE GAME:_
![Spelling Bee](https://github.com/yazenoubari/NYT_spelling_bee_solver/blob/main/spelling_bee.png)

### <Br> _Finding all the possible words that fit these constraints and rules:_
- Words must contain at least 4 letters.
- Words must include the center letter.
- Our word list does not include words that are obscure, hyphenated, or proper nouns.
- Letters can be used more than once. <Br>
### Score points to increase your rating:
- 4-letter words are worth 1 point each.
- Longer words earn 1 point per letter.
- Each puzzle includes at least one “panagram” which uses every letter. These are worth 7 extra points!
- 
  ###### _Note: Not all words are valid, despite being officially recognized._ <Br>
  ##### From the New York Times:
  > ###### "Every Spelling Bee puzzle is hand-curated to focus on relatively common words (with a couple tough ones here and there to keep it challenging).
  > ###### ...The key word here is 'common'. We just removed words from Spelling Bee that we believe are not common enough, despite them being defined words, in order to maintain a level-field for all of our solvers." 
***
### _USING THE PROGRAM:_
#### __Manual Mode:__
> All user input is done through the console.
> 1. Type 'na' into the console to go into manual mode.
> 2. Input the center letter of the Bee.
> 2. Input the 6 outer letters that surround the center letter.

#### __Semi-Automatic Mode:__
> 1. Take a screenshot of the game, while excluding any text other than the 7 letters of the game. 
>   - ##### ***Only [.png], [.jpg], or [.tif] file extentions are permitted.***
> 2. Paste your image path into the program. There is a dedicated folder to save your images in the /src/resources/images directory in the project root.
>   - ##### ***If the image path cannot be followed or the file cannot be found, the program will throw an error, and force the user into manual mode.***
>   
***
### _THE WORKINGS of the program_:
#### The Logic:
> The inputted characters are assigned into a char arrayList. This is an expandable memory set, in order to stay unrestrained by character count. The char arrayList is then cycled through and compared to each letter of a word. If there is a character match, an integer variable, e.g., 'int' increases by one. If 'int' is the same size as the length of that word, then we know that the word was only composed of letters found in that character list. This cycle is then continued for every word in the dictionary.
> <Br>All Unix-like systems contain a .txt file with an official list of English words. These words are then transferred line-by-line into an arrayList. Each array index will correspond to an English word.
> 
#### The OCR:
> The program uses Google's Tesseract OCR to extract the letters from the screenshot. <Br>
> Originally, I was hoping that Tesseract could identify all 7 of the characters from the raw image, though, due to how the letters were scattered, it could only identify one character at a time——this proved a big challenge for me. The most immediate solution was to singly create an image for each one of the letters (7 images total, for 7 letters) and feed those, individually, into the OCR.
> <Br>To recognize the location of each of the letters in any random image of the game board, the program had to be cropped to known proportions.<Br>
> #### Here was the process:
> 1. Grayscale the raw image. <Br>
>
>![Gray Scale](https://github.com/yazenoubari/NYT_spelling_bee_solver/blob/yazenoubari-patch-2/grayscale.png)
>
> 2. Remove the haziness and unwanted shadings in the photo: replace some darker grey shades to black, and any other lighter shades to white. This creates a binary black-white image, which is easier to process.<Br>
> 
>![Binary](https://github.com/yazenoubari/NYT_spelling_bee_solver/blob/yazenoubari-patch-2/final.png)
>
> 3. Crop the image from each side: create an array of all the pixel colors of the image. Then cycle through each pixel by row or column until it encounters a black-colored pixel. The row/column index is saved, and the image is trimmed at that location. This process is repeated four times along each image border.<Br>
>
>![Cropped](https://github.com/yazenoubari/NYT_spelling_bee_solver/blob/yazenoubari-patch-2/cropped.png)
>
> 4. Create images of each of the letters: give the cartesian location of each letter using it relationship to the image's length and width. These proportions will vary for every letter. E.g. an letter in the center of an image is at ''0.5 * width'' of image.<Br>
> 
> ![A](https://github.com/yazenoubari/NYT_spelling_bee_solver/blob/yazenoubari-patch-3/r_TOP.png)
> ![V](https://github.com/yazenoubari/NYT_spelling_bee_solver/blob/yazenoubari-patch-3/r_BOT.png)
> ![C](https://github.com/yazenoubari/NYT_spelling_bee_solver/blob/yazenoubari-patch-3/l_TOP.png)
> ![P](https://github.com/yazenoubari/NYT_spelling_bee_solver/blob/yazenoubari-patch-3/l_BOT.png)
> ![L](https://github.com/yazenoubari/NYT_spelling_bee_solver/blob/yazenoubari-patch-3/c_TOP.png)
> ![T](https://github.com/yazenoubari/NYT_spelling_bee_solver/blob/yazenoubari-patch-3/c_MID.png)
> ![E](https://github.com/yazenoubari/NYT_spelling_bee_solver/blob/yazenoubari-patch-3/c_BOT.png)
> 
> 5. Send all 7 of the images through the Tesseract OCR, retrieve the letters, and send to the logic portion of the program.
> 
