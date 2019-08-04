# -*- coding: utf-8 -*-
"""
Spyder Editor

This is a temporary script file.
"""

import re
import os


def main():
    filename = 'log.txt'

    pathname = "./out-python/"

    os.makedirs(os.path.dirname(pathname), exist_ok=True)

    saveFileMarcados = open('./out-python/marcados.txt', 'w')
    saveFileDisparo = open('./out-python/disparos.txt', 'w')
    saveFileOutput = open('./out-python/output.txt', 'w')

    try:
        file = open(filename, 'r')
    except IOError:
        print('error file')

    marcados = []
    disparos = []

    for line in file:
        regexMarcado = re.match(r'marcado=\[([\d+, ]+)\]', line)
        regexDisparo = re.match(r'disparo=(\d+)', line)

        if regexMarcado:
            marcados.append(regexMarcado.group(1))

        if regexDisparo:
            disp = '' + regexDisparo.group(1)
            disparos.append(int(disp))

    for i in range(len(marcados)):
        saveFileMarcados.write('%s\n' % marcados[i])

    for i in range(len(disparos)):
        saveFileDisparo.write('%s,' % disparos[i])

    saveFileMarcados.close()
    saveFileDisparo.close()

    tinv = [[0, 10, 11, 12, 14, 2, 9],
            [0, 10, 13, 2, 9],
            [0, 1, 3, 4, 6],
            [0, 1, 3, 4, 5, 7, 8]]

    print(disparos)
    saveFileOutput.write('INPUT\n')
    saveFileOutput.write(str(disparos) + '\n')

    for j in range(len(tinv)):
        print(j)
        saveFileOutput.write(
            '-----------------------------------------------------------------------------------------\n')
        saveFileOutput.write(
            '#######################################################################################' + str(j) + '\n')
        saveFileOutput.write(
            '#######################################################################################' + str(j) + '\n')
        saveFileOutput.write(
            '#######################################################################################' + str(j) + '\n')
        saveFileOutput.write(
            '#######################################################################################' + str(j) + '\n')
        saveFileOutput.write(
            '#######################################################################################' + str(j) + '\n')
        saveFileOutput.write(
            '-----------------------------------------------------------------------------------------\n')

        disparoInArray = True
        #        loop 1 arreglo hasta que termine de borrar todas las ocurrencias del conjunto total
        while disparoInArray:
            deleteSet = False
            for i in range(len(tinv[j])):
                if tinv[j][i] in disparos:
                    deleteSet = True
                    continue
                else:
                    disparoInArray = False
                    deleteSet = False
                    break

            if deleteSet:
                for i in tinv[j]:
                    try:
                        print('removing' + str(i))
                        saveFileOutput.write('removing' + str(i) + '\n')
                        disparos.remove(i)
                    except ValueError:
                        pass

    # imprimiendo resultado
    saveFileOutput.write('RESULTS\n')
    saveFileOutput.write(str(disparos) + '\n')
    print(disparos)
    saveFileOutput.close()

    saveDisparoTINV = open('./out-python/disparos_tinv.txt', 'w')
    for i in range(len(disparos)):
        saveDisparoTINV.write('%s,' % disparos[i])
    saveDisparoTINV.close()


if __name__ == "__main__":
    main()
