#!/usr/bin/python

import json
import sqlite3
import os

# Enums
class Noun:
  TABLE = "nouns"
  ID = "_id"
  HANZI = "hanzi"
  PINYIN = "pinyin"
  ENGLISH = "english"
class MeasureWord:
  TABLE = "measure_words"
  ID = "_id"
  HANZI = "hanzi"
  PINYIN = "pinyin"
  ENGLISH = "english"
class Join:
  TABLE = "nouns_measure_words"
  ID = "_id"
  NOUN_ID = "noun_id"
  MEASURE_WORD_ID = "measure_word_id"
  CORRECT = "correct"
  INCORRECT = "incorrect"

# Create DB
FILENAME = 'measure_words.sqlite3'

if os.path.exists(FILENAME):
  os.unlink(FILENAME)
cur = sqlite3.connect(FILENAME).cursor()

data = 'CREATE TABLE "android_metadata" ("locale" TEXT DEFAULT "en_US")'
print data
cur.execute(data)

data = 'INSERT INTO "android_metadata" VALUES ("en_US")'
print data
cur.execute(data)

data = ('CREATE TABLE "' + Noun.TABLE + '" (' +
  '"' + Noun.ID + '" INT PRIMARY KEY, ' +
  '"' + Noun.HANZI + '" TEXT, ' +
  '"' + Noun.PINYIN + '" TEXT, ' +
  '"' + Noun.ENGLISH + '" TEXT)')
print data
cur.execute(data)

data = ('CREATE TABLE "' + MeasureWord.TABLE + '" (' +
  '"' + MeasureWord.ID + '" INT PRIMARY KEY, ' +
  '"' + MeasureWord.HANZI + '" TEXT, ' +
  '"' + MeasureWord.PINYIN + '" TEXT, ' +
  '"' + MeasureWord.ENGLISH + '" TEXT)')
print data
cur.execute(data)

data = ('CREATE TABLE "' + Join.TABLE + '" (' +
  '"' + Join.ID + '" INT PRIMARY KEY, ' +
  '"' + Join.NOUN_ID + '" INT, ' +
  '"' + Join.MEASURE_WORD_ID + '" INT, ' +
  '"' + Join.CORRECT + '" INT DEFAULT 0, '
  '"' + Join.INCORRECT + '" INT DEFAULT 0, ' +
  'FOREIGN KEY ("' + Join.NOUN_ID + '") REFERENCES "' +
      Noun.TABLE + '" ("' + Noun.ID + '"), ' +
  'FOREIGN KEY ("' + Join.MEASURE_WORD_ID + '") REFERENCES "' +
      MeasureWord.TABLE + '" ("' + MeasureWord.ID + '"))')
print data
cur.execute(data)

# Read data into memory
with open('nouns.json') as f:
  nouns = json.load(f)
with open('measure_words.json') as f:
  measure_words = json.load(f)
with open('nouns_measure_words.json') as f:
  nouns_measure_words = json.load(f)
