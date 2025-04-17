import cv2
import pickle
import mediapipe as mp
import numpy as np

# Load the model
model_dict = pickle.load(open('./model.p', 'rb'))
model = model_dict['model']

# Initialize MediaPipe hands module
mp_hands = mp.solutions.hands
mp_drawing = mp.solutions.drawing_utils
mp_drawing_styles = mp.solutions.drawing_styles

hands = mp_hands.Hands(static_image_mode=False, min_detection_confidence=0.3, min_tracking_confidence=0.3)


def process_frame(frame):

    frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

    results = hands.process(frame_rgb)

    if results.multi_hand_landmarks:
        data_aux = []
        x_ = []
        y_ = []

        hand_landmarks = results.multi_hand_landmarks[0]

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

        if len(data_aux) == 42:

            prediction = model.predict([np.asarray(data_aux)])
            return prediction[0], results.multi_hand_landmarks
        else:
            return None, None
    else:
        return None, None


if __name__ == "__main__":

    cap = cv2.VideoCapture(0)

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            print("Failed to capture frame")
            break

        predicted_character, hand_landmarks = process_frame(frame)

        if hand_landmarks:
            for landmarks in hand_landmarks:

                mp_drawing.draw_landmarks(
                    frame,
                    landmarks,
                    mp_hands.HAND_CONNECTIONS,
                    mp_drawing_styles.get_default_hand_landmarks_style(),
                    mp_drawing_styles.get_default_hand_connections_style()
                )

        if predicted_character:
            cv2.putText(frame, f"Predicted: {predicted_character}", (10, 50),
                        cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 0, 0), 2, cv2.LINE_AA)


        cv2.imshow("Hand Detection", frame)

        # Break loop on 'q' key press
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break


    cap.release()
    cv2.destroyAllWindows()
