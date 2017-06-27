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
import os
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
from shutil import copyfile


if __name__ == "__main__":
    # NOTE: we put the following in a 'if __name__ == "__main__"' protected
    # block to be able to use a multi-core grid search that also works under
    # Windows, see: http://docs.python.org/library/multiprocessing.html#windows
    # The multiprocessing module is used as the backend of joblib.Parallel
    # that is used when n_jobs != 1 in GridSearchCV

    # the training data folder must be passed as first argument
    # all the files in this folder is the training data

    print len(sys.argv)
    print os.getcwd()


    if len(sys.argv) == 1:
        balence_data_folder = "predict_page/dataset/bal_dataset_1500"
        # API_NAME = sys.argv[1].split('://')[-1].split('/')[0]
        # if "google" in API_NAME:
        #     API_NAME = sys.argv[1].split('://')[-1].split('/')[1]
        external_data_folder = "EntrySet/" + "external_dataset"

    elif len(sys.argv) == 2:
        balence_data_folder = "predict_page/dataset/bal_dataset_1500"
        # API_NAME = sys.argv[1].split('://')[-1].split('/')[0]
        # if "google" in API_NAME:
        #     API_NAME = sys.argv[1].split('://')[-1].split('/')[1]
        external_data_folder = "EntrySet/" + "external_dataset"

    elif len(sys.argv) == 3:
        print "only two parameters"
        exit(0)

    # Create folder for FilteredSet
    filteredSet = "FilteredSet/" + "external_dataset"


    # set shuffle = True
    dataset = load_files(balence_data_folder, shuffle=True)
    print("train_samples: %d" % len(dataset.data))

    # test external folder
    test_set = load_files(external_data_folder, shuffle=True)
    print("external_test_samples: %d" % len(test_set.data))

    # Use Random forest max_depth=5, n_estimators=10, max_features=1
    pipeline = Pipeline([('vect', TfidfVectorizer(min_df=3, max_df=0.95, decode_error='ignore')),
                         ('clf', RandomForestClassifier()), ])

    # TASK: Build a grid search to find out whether unigrams or bigrams are
    # more useful.
    # Fit the pipeline on the training set using grid search for the parameters
    # parameters = {'vect__ngram_range': [(1,1),(1,2)],}
    parameters = {'vect__ngram_range': [(1,1)],}
    grid_search = GridSearchCV(pipeline, parameters, n_jobs=-1, cv=10)
    grid_search.fit(dataset.data, dataset.target)
    # TASK: print the cross-validated scores for the each parameters set
    # explored by the grid search
    n_candidates = len(grid_search.cv_results_['params'])
    for i in range(n_candidates):
        print(i, 'params - %s; mean - %0.2f; std -%0.2f'
                % (grid_search.cv_results_['params'][i],
                   grid_search.cv_results_['mean_test_score'][i],
                   grid_search.cv_results_['std_test_score'][i]))

    # TASK: Predict the outcome on the testing set and store it in a variable named y_predicted
    y_predicted = grid_search.predict(test_set.data)
    # Print the classification report
    report = metrics.classification_report(test_set.target, y_predicted,
                                        target_names=dataset.target_names)
    print report

    # Write report to the file
    if not os.path.exists(filteredSet):
        os.makedirs(filteredSet)
    report_name = filteredSet + "/balance_external_1500.txt"
    f = open(report_name, 'w+')
    f.write(report)


    # Show the details results for each file
    for i in range(0, len(test_set.data)):
        # print('File "%s" is "%d": predicted "%d"' % (test_set.filenames[i], test_set.target[i], y_predicted[i]))
        if (test_set.target[i] != y_predicted[i] ):
            # Write filtered file into FilteredSet
            print('Different: File "%s" is "%d": predicted "%d"' % (test_set.filenames[i], test_set.target[i], y_predicted[i]))
            # move file to the new FilteredSet
        else:
            copyfile(test_set.filenames[i], filteredSet + "/" + test_set.filenames[i].split("/")[-1])
