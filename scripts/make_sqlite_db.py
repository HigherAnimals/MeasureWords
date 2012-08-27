#!/usr/bin/python

import json
import sqlite3
import os
import sys

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
FILENAME = 'assets/measure_words.sqlite3'

if os.path.exists(FILENAME):
  os.unlink(FILENAME)
con = sqlite3.connect(FILENAME)
cur = con.cursor()

USER_VERSION = 1
qry = 'PRAGMA user_version = %s;' % USER_VERSION
cur.execute(qry)

qry = 'CREATE TABLE "android_metadata" ("locale" TEXT DEFAULT "en_US");'
print qry
cur.execute(qry)

qry = 'INSERT INTO "android_metadata" VALUES ("en_US");'
print qry
cur.execute(qry)

qry = ('CREATE TABLE "%s" ("%s" INTEGER PRIMARY KEY AUTOINCREMENT, "%s" TEXT, "%s" TEXT, "%s" TEXT);' % (
  Noun.TABLE, Noun.ID, Noun.HANZI, Noun.PINYIN, Noun.ENGLISH
))
print qry
cur.execute(qry)

qry = ('CREATE TABLE "%s" ("%s" INTEGER PRIMARY KEY AUTOINCREMENT, "%s" TEXT, "%s" TEXT, "%s" TEXT);' % (
  MeasureWord.TABLE, MeasureWord.ID, MeasureWord.HANZI,
  MeasureWord.PINYIN, MeasureWord.ENGLISH
))
print qry
cur.execute(qry)

qry = ('CREATE TABLE "%s" ("%s" INTEGER PRIMARY KEY AUTOINCREMENT, "%s" INTEGER, "%s" INTEGER, "%s" INTEGER DEFAULT 0, "%s" INTEGER DEFAULT 0, FOREIGN KEY ("%s") REFERENCES "%s" ("%s"), FOREIGN KEY ("%s") REFERENCES "%s" ("%s"));' % (
  Join.TABLE, Join.ID,
  Join.NOUN_ID, Join.MEASURE_WORD_ID,
  Join.CORRECT, Join.INCORRECT,
  Join.NOUN_ID, Noun.TABLE, Noun.ID,
  Join.MEASURE_WORD_ID, MeasureWord.TABLE, MeasureWord.ID
));
print qry
cur.execute(qry)

# Read data into db
with open('data/nouns.json') as f:
  for noun in json.load(f):
    qry = ('INSERT INTO "%s" ("%s", "%s", "%s") VALUES ("%s", "%s", "%s");' % (
      Noun.TABLE, Noun.HANZI, Noun.PINYIN, Noun.ENGLISH,
      noun[Noun.HANZI], noun[Noun.PINYIN], noun[Noun.ENGLISH]
    ));
    print qry
    cur.execute(qry)
    con.commit()
     
with open('data/measure_words.json') as f:
  for measure_word in json.load(f):
    qry = ('INSERT INTO "%s" ("%s", "%s", "%s") VALUES ("%s", "%s", "%s");' % (
      MeasureWord.TABLE, MeasureWord.HANZI,
      MeasureWord.PINYIN, MeasureWord.ENGLISH,
      measure_word[MeasureWord.HANZI], measure_word[MeasureWord.PINYIN],
      measure_word[MeasureWord.ENGLISH]
    ));
    print qry
    cur.execute(qry)
    con.commit()

with open('data/nouns_measure_words.json') as f:
  for noun_measure_word in json.load(f):
    # Get noun id
    noun_hanzi = noun_measure_word['noun']
    cur.execute('SELECT "%s" FROM "%s" WHERE %s = "%s";' % (
      Noun.ID, Noun.TABLE, Noun.HANZI, noun_hanzi
    ))
    noun_id = cur.fetchone()[0]
    # Get measure words id
    measure_word_hanzi = noun_measure_word['measure_word']
    cur.execute('SELECT "%s" FROM "%s" WHERE "%s" = "%s";' % (
      MeasureWord.ID, MeasureWord.TABLE, MeasureWord.HANZI, measure_word_hanzi
    ))
    measure_word_id = cur.fetchone()[0]
    # Insert
    cur.execute('INSERT INTO "%s" ("%s", "%s") VALUES ("%s", "%s");' %(
      Join.TABLE, Join.NOUN_ID, Join.MEASURE_WORD_ID, noun_id, measure_word_id
    ))
    con.commit()
    
con.commit()
con.close()
