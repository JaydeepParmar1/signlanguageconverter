import os
import cv2
import string

DATA_DIR = './data'
if not os.path.exists(DATA_DIR):
    os.makedirs(DATA_DIR)

alphabet_labels = list(string.ascii_uppercase)
dataset_size = 500


cap = cv2.VideoCapture(0)

for label in alphabet_labels:
    folder_path = os.path.join(DATA_DIR, label)
    if not os.path.exists(folder_path):
        os.makedirs(folder_path)

    print(f'Collecting data for class {label}')


    while True:
        ret, frame = cap.read()
        cv2.putText(frame, 'Ready? Press "Q" ! :)', (100, 50), cv2.FONT_HERSHEY_SIMPLEX, 1.3, (0, 255, 0), 3,
                    cv2.LINE_AA)
        cv2.imshow('frame', frame)
        if cv2.waitKey(25) == ord('q'):
            break


    counter = 0
    while counter < dataset_size:
        ret, frame = cap.read()
        cv2.imshow('frame', frame)
        cv2.waitKey(25)


        image_path = os.path.join(folder_path, f'{counter}.jpg')
        cv2.imwrite(image_path, frame)
        counter += 1

# Release resources
cap.release()
cv2.destroyAllWindows()
