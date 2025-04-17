import pickle
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score
import numpy as np
from sklearn.svm import SVC


data_dict = pickle.load(open('./data.pickle', 'rb'))


raw_data = data_dict['data']
labels = data_dict['labels']


max_length = max(len(item) for item in raw_data)
padded_data = [
    item + [0] * (max_length - len(item)) if len(item) < max_length else item
    for item in raw_data
]


data = np.array(padded_data)
labels = np.asarray(labels)


x_train, x_test, y_train, y_test = train_test_split(
    data, labels, test_size=0.2, shuffle=True, stratify=labels
)
model = SVC(probability=True)

model.fit(x_train, y_train)


y_predict = model.predict(x_test)


score = accuracy_score(y_predict, y_test)
print(f"{score * 100:.2f}% of samples were classified correctly!")


with open('model.p', 'wb') as f:
    pickle.dump({'model': model}, f)

