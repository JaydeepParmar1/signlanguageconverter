import sys
import pickle
import cv2
import mediapipe as mp
import numpy as np


model_dict = pickle.load(open('./model.p', 'rb'))
model = model_dict['model']


mp_hands = mp.solutions.hands
mp_drawing = mp.solutions.drawing_utils
mp_drawing_styles = mp.solutions.drawing_styles

hands = mp_hands.Hands(static_image_mode=True, min_detection_confidence=0.3)


def process_image(image_path):

    frame = cv2.imread(image_path)


    frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)


    results = hands.process(frame_rgb)


    if results.multi_hand_landmarks:
        data_aux = []
        x_ = []
        y_ = []

        for hand_landmarks in results.multi_hand_landmarks:

            for i in range(len(hand_landmarks.landmark)):
                x = hand_landmarks.landmark[i].x
                y = hand_landmarks.landmark[i].y
                x_.append(x)
                y_.append(y)

            for i in range(len(hand_landmarks.landmark)):
                x = hand_landmarks.landmark[i].x
                y = hand_landmarks.landmark[i].y
                data_aux.append(x - min(x_))
                data_aux.append(y - min(y_))


        prediction = model.predict([np.asarray(data_aux)])


        return prediction[0]
    else:
        return None


if __name__ == "__main__":
    image_path = "uploaded_image/captured_image.jpg"
    predicted_character = process_image(image_path)
    if predicted_character:
        print(predicted_character)
    else:
        print("No hand detected")
