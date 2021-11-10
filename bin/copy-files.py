#!/usr/bin/env python3

import sqlite3
import shutil
import argparse
import os

def copy_files(db, raw, tgt_folder):    
    conn = sqlite3.connect(db)
    cur  = conn.cursor()
    fetchall = cur.execute('select mo.id, mo.filename from models mo join metadata mm on mo.id = mm.id');

    for m in fetchall:
        relative_file = m[1]
        src = os.path.join(raw, relative_file)
        dst = os.path.join(tgt_folder, relative_file)

        print("Copying ", src, " to ", dst)
        file_folder = os.path.dirname(dst)
        if not os.path.exists(file_folder):
            os.makedirs(file_folder)

        shutil.copyfile(src, dst)

def parse_args():
    parser = argparse.ArgumentParser(description='Download files from github.')
    parser.add_argument('root', metavar='ROOT', type=str,
                   help='The modelset root folder')
    args = parser.parse_args()
    return args

if __name__ == "__main__":
    args = parse_args()
    # Copy Ecore
    db_ecore  = os.path.join(args.root, "datasets/dataset.ecore/data/ecore.db")
    raw_ecore = os.path.join(args.root, "raw-data/repo-ecore-all")
    tgt_folder = "/tmp/modelset/raw-data/repo-ecore-all"

    if not os.path.exists(tgt_folder):
        os.makedirs(tgt_folder)
    
    copy_files(db_ecore, raw_ecore, tgt_folder)

    # Copy UML
    db_uml  = os.path.join(args.root, "datasets/dataset.genmymodel/data/genmymodel.db")
    raw_uml = os.path.join(args.root, "raw-data/repo-genmymodel-uml")
    tgt_folder = "/tmp/modelset/raw-data/repo-genmymodel-uml"

    if not os.path.exists(tgt_folder):
        os.makedirs(tgt_folder)
    
    copy_files(db_uml, raw_uml, tgt_folder)
    
