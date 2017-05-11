"""Build a sentiment analysis / polarity model

Sentiment analysis can be casted as a binary text classification problem,
that is fitting a linear classifier on features extracted from the text
of the user messages so as to guess wether the opinion of the author is
positive or negative.

In this examples we will use a movie review dataset.

"""
# Author: Olivier Grisel <olivier.grisel@ensta.org>
# License: Simplified BSD

import sys
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.svm import LinearSVC
from sklearn.pipeline import Pipeline
from sklearn.model_selection import GridSearchCV
from sklearn.datasets import load_files
from sklearn.model_selection import train_test_split
from sklearn import metrics
import matplotlib.pyplot as plt
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import cross_val_predict


if __name__ == "__main__":
    # NOTE: we put the following in a 'if __name__ == "__main__"' protected
    # block to be able to use a multi-core grid search that also works under
    # Windows, see: http://docs.python.org/library/multiprocessing.html#windows
    # The multiprocessing module is used as the backend of joblib.Parallel
    # that is used when n_jobs != 1 in GridSearchCV

    # the training data folder must be passed as first argument
    unbalence_data_folder = "dataset/unbal_dataset"
    dataset = load_files(unbalence_data_folder, shuffle=True)
    print("n_samples: %d" % len(dataset.data))
    # split the dataset in training and test set:
    docs_train, docs_test, y_train, y_test = train_test_split(
        dataset.data, dataset.target, test_size=0.20, random_state=None)

    # TASK: Build a vectorizer / classifier pipeline that filters out tokens
    # that are too rare or too frequent
    # --------- encode issue ----------------

    # Use Random forest max_depth=5, n_estimators=10, max_features=1
    pipeline = Pipeline([('vect', TfidfVectorizer(min_df=3, max_df=0.95, decode_error='ignore')),
                         ('clf', RandomForestClassifier()), ])

    # # Cross validation
    # predicted = cross_val_predict(pipeline, dataset.data, dataset.target, cv=10)
    # print metrics.accuracy_score(dataset.target, predicted)

    # TASK: Build a grid search to find out whether unigrams or bigrams are
    # more useful.
    # Fit the pipeline on the training set using grid search for the parameters
    parameters = {'vect__ngram_range': [(1,1),(1,2)],}
    grid_search = GridSearchCV(pipeline, parameters, n_jobs=-1)
    grid_search.fit(docs_train, y_train)
    # TASK: print the cross-validated scores for the each parameters set
    # explored by the grid search
    n_candidates = len(grid_search.cv_results_['params'])
    for i in range(n_candidates):
        print(i, 'params - %s; mean - %0.2f; std -%0.2f'
                % (grid_search.cv_results_['params'][i],
                   grid_search.cv_results_['mean_test_score'][i],
                   grid_search.cv_results_['std_test_score'][i]))
    # TASK: Predict the outcome on the testing set and store it in a variable
    # named y_predicted
    y_predicted = grid_search.predict(docs_test)
    # Print the classification report
    report = metrics.classification_report(y_test, y_predicted,
                                        target_names=dataset.target_names)
    print report
    # Write report to the file
    report_name = "unbalance_internal.txt"
    f = open(report_name, 'w+');
    f.write(report)
    # # Show the details results for each file
    # for i in range(0, len(docs_test) - 1):
    #     print('File "%s" is "%d": predicted "%d"' % (docs_test.filenames[i], y_test[i], y_predicted[i]))
    #     if (test_set.target[i] != y_predicted[i] ):
    #         file_name = str(i) + '.html'
    #         f = open(file_name, 'w+')
    #         f.write(test_set.data[i])