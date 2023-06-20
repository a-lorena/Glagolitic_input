import cv2
import os
import glob
import sys
from directory_name import *


letter_number = input("Directory number: ")
letter = input("Letter: ")
directory = "ImageDataset/" + letter_number + "/*.png"
save_directory = DATA_DIR + letter
n = 2

###### LIST OF IMAGES ######
images = [cv2.imread(image) for image in glob.glob(directory)]

if (os.mkdir(save_directory)):
	print("Directory created")
os.chdir(save_directory)

i = 1
number_of_images = len(images)
result_number_of_images = number_of_images * 9
current_number_of_images = 0
percent = 0

for image in images:
	image_bw = cv2.bitwise_not(image)
	new_image_name = str(letter_number) + "_" + str(i) + '.png'

	if (cv2.imwrite(new_image_name, image_bw)):
		current_number_of_images += 1
	else:
		print("Failed to write: " + new_image_name)

	# EROSION
	for j in range(0, n):
		eroded = cv2.erode(image_bw.copy(), None, iterations=j + 1)
		new_image_name = str(letter_number) + "_" + str(i) + "_e_" + str(j) + '.png'

		if (cv2.imwrite(new_image_name, eroded)):
			current_number_of_images += 1
		else:
			print("Failed to write: " + new_image_name)

	# DILATION
	for j in range(0, n):
		dilated = cv2.dilate(image_bw.copy(), None, iterations=j + 2)
		new_image_name = str(letter_number) + "_" + str(i) + "_d_" + str(j) + '.png'

		if (cv2.imwrite(new_image_name, dilated)):
			current_number_of_images += 1
		else:
			print("Failed to write: " + new_image_name)

	(height, width) = image_bw.shape[:2]
	(centerX, centerY) = (width // 2, height // 2)
	rotation_left = [5, 10]
	rotation_right = [-5, -10]

	# ROTATE LEFT
	for j in range(0, 2):
		M = cv2.getRotationMatrix2D((centerX, centerY), rotation_left[j], 1.0)
		rotated_left = cv2.warpAffine(image_bw, M, (width, height))

		new_image_name = str(letter_number) + "_" + str(i) + "_rl_" + str(j) + '.png'
		if (cv2.imwrite(new_image_name, rotated_left)):
			current_number_of_images += 1
		else:
			print("Failed to write: " + new_image_name)

	# ROTATE RIGHT
	for j in range(0, 2):
		M = cv2.getRotationMatrix2D((centerX, centerY), rotation_right[j], 1.0)
		rotated_right = cv2.warpAffine(image_bw, M, (width, height))

		new_image_name = str(letter_number) + "_" + str(i) + "_rr_" + str(j) + '.png'
		if (cv2.imwrite(new_image_name, rotated_right)):
			current_number_of_images += 1
		else:
			print("Failed to write: " + new_image_name)

	# PRINT PROGRESS
	sys.stdout.write('\r')	
	percent = int((current_number_of_images/result_number_of_images) * 100)
	sys.stdout.write("Progress: " + str(percent) + "%")

	i += 1

# OPERATIONS DONE
sys.stdout.write("\nDirectory " + str(letter) + " done\n")
sys.exit()