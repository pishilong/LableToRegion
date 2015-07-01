#!/usr/bin/python

import string;


labelsCount_total = 0;
labelsCount_correct = 0;

MAX_FILE_INDEX = 100

referenceResultList = {} #reference l2r result, key type: int
myTotalResultList = {}   #our .his result, key type:int

finalHistogramLocation = './website/public/regionLabel/regionLabel.his'

#read in our experiment result from .his file


#readin our final regionLabel.his file
filename = raw_input('please input the result .his file path(default to %s)\n:' %finalHistogramLocation);
if (filename.strip() == ''):
	filename = finalHistogramLocation;
print filename;
file_his = open(filename, 'rb');
print "name of the file:",file_his.name;
lines = file_his.readlines();

lastFileIndex = 1;
lastFileRegionLabelMapping = {};
for lineNum in range(0, len(lines)):
	line = lines[lineNum];
	line = line[0:-1];#remove last return character
	lineSplit = line.split(',');
	# print lineSplit;

	fileIndex = string.atoi(lineSplit[0]);
	regionVector = lineSplit[1].split(' ');
	# print regionVector;save file 
	regionIndex = string.atoi(regionVector[0]);
	finalLabel = regionVector[len(regionVector)-1];

	if fileIndex > MAX_FILE_INDEX:#only get 100 files result
		break;
	
	# print 'fileIndex: %s regionIndex:%s finalLabel:%s' %(fileIndex,regionIndex,finalLabel);

	if lastFileIndex != fileIndex:
		print "lastFileIndex:%d fileIndex:%d" %(lastFileIndex , fileIndex);
		print "save file %d labels mappings:" %fileIndex;
		print lastFileRegionLabelMapping;
		#add file-based result into total result dictionary
		myTotalResultList[lastFileIndex] = (lastFileRegionLabelMapping);
		lastFileRegionLabelMapping = {};
		
	lastFileRegionLabelMapping[regionIndex] = finalLabel;
	lastFileIndex = fileIndex;

#do not forget the last item
myTotalResultList[lastFileIndex] = (lastFileRegionLabelMapping);

file_his.close()
# print myTotalResultList;
# print "myTotalResultList keys count:%d" %len(myTotalResultList.keys());
# raw_input();

print '=========================Finish parsing RESULT histogram============================================'

#read in first 100 mask and l2r result
for fileIndex in xrange(1,MAX_FILE_INDEX+1):
	filename_mask = 'mask/%d.mask' %fileIndex ;
	filename_l2r = 'groundtruth/%d.l2r' %fileIndex;
	print "processing:" + filename_mask + ' ' + filename_l2r;

	file_mask = open(filename_mask);
	file_l2r = open(filename_l2r);
	linesOfMask = file_mask.readlines();
	linesOfL2r = file_l2r.readlines();
	if len(linesOfMask) != len(linesOfL2r):
		print 'line count differ between ', filename_mask, ' and ', filename_l2r
		continue;

	regionLabelMapping = {};
	for i in range(len(linesOfMask)):
		lineMask = linesOfMask[i];
		lineL2r = linesOfL2r[i];
		lineMaskArray = lineMask.split();
		lineL2rArray = lineL2r.split();
		column = len(lineMaskArray);
		#update my region label mapping 
		for c in range(column):
			key = string.atoi(lineMaskArray[c]);
			regionLabelMapping[key] = lineL2rArray[c];


	# print "image %d label mapping:" %fileIndex
	# print  regionLabelMapping;
	referenceResultList[fileIndex] = (regionLabelMapping);

	file_mask.close();
	file_l2r.close();

print '====================================================================='
# print referenceResultList;


#compare with our result with references
labelAccuracyMap = {}
mostCorrectFileRate = 0.0;
mostCorrectFileIndex = -1;
for fileIndex in range(1, len(myTotalResultList.keys())+1):

	fileLabelMappingDictRef = referenceResultList[fileIndex];
	fileLabelMappingDictTest = myTotalResultList[fileIndex];
	key1Count = len(fileLabelMappingDictRef.keys());
	key2Count = len(fileLabelMappingDictTest.keys());
	# print "refKeysCount:%d resultKeyCount:%d" %(key1Count, key2Count);
	if key1Count != key2Count:
		continue;

	fileCorrectRate = 0.0;
	fileLabelCorrectCount = 0;
	for regionId in fileLabelMappingDictRef.keys():
		str = 'processing key %s in image%s' %(regionId, fileIndex);
		labelsCount_total = labelsCount_total+1;

		resultLabel = fileLabelMappingDictRef[regionId]
		targetLabel = fileLabelMappingDictTest[regionId]
		correct = (resultLabel == targetLabel)
		if correct:
			str += " correct!"
			#total correct count +1
			labelsCount_correct = labelsCount_correct+1;
			#file correct count +1
			fileLabelCorrectCount += 1;

		#update label based accuray data 
		if labelAccuracyMap.has_key(targetLabel) == 0:
			labelAccuracyMap[targetLabel] = {'total':0, 'correct':0}
		if labelAccuracyMap.has_key(resultLabel) == 0:
			labelAccuracyMap[resultLabel] = {'total':0, 'correct':0}
		currLabelData = labelAccuracyMap[targetLabel]
		if correct:
			currLabelData['total'] = currLabelData['total'] + 1
			currLabelData['correct'] = currLabelData['correct'] + 1
		else:
			anotherLabelData = labelAccuracyMap[resultLabel]
			anotherLabelData['total'] = anotherLabelData['total'] + 1


		print str;

	#calc file-base correct rate
	fileCorrectRate = float(fileLabelCorrectCount) / len(fileLabelMappingDictRef.keys());
	if fileCorrectRate > mostCorrectFileRate:
		mostCorrectFileRate = fileCorrectRate;
		mostCorrectFileIndex = fileIndex;
	print "========file:%d.jpg correct percentage:%f========" %(fileIndex, fileCorrectRate);
			
for labelI in labelAccuracyMap.keys():
	labelICount = labelAccuracyMap[labelI]['total']
	labelICorrect = labelAccuracyMap[labelI]['correct']
	print '# %s label accurary:%f (%d/%d)' %(labelI, float(labelICorrect)/labelICount,labelICorrect,labelICount)

print "The most correct file:%d.jpg correct percentage:%f" %(mostCorrectFileIndex, mostCorrectFileRate);

print "TotalLabels:%d correctLabels:%d total correct percentage:%f" %(labelsCount_total, labelsCount_correct, 
float(labelsCount_correct)/float(labelsCount_total));	



