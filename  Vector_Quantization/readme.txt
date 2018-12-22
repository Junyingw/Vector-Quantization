This assignment will increase your understanding of image compression. 

Your result should present images side by side – original and output. The output would be computed depending on three parameters:

• An input image, all images are standard CIF size 352x288 and supplied in two formats – myImage.raw (if it is a single channel 8 bit per pixel image) or myImage.rgb (if it is a color three channel 24 bits per pixel image). Please refer to the readme with the images for the format. Your code in assignment 1 can be used to display these images.

• N which gives you several vectors for quantization, so that each vector in the input can ultimately use log2N bits. Expect this input to be a power of 2.

• Mode, which suggests how pixels should be grouped to form vectors. For this assignment, these are the following values the variable can take:

o 1 – suggesting two side by side pixels (whether gray or color) form a vector
o 2 – suggesting a 2x2 block of pixels (whether gray or color) form a vector 
o 3 – suggesting a 4x4 block of pixels (whether gray or color) form a vector