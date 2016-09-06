

def main():
  f = open('servers.txt', 'w')
  for value in range(1600, 1811):
    value = str(value) + "\n"
    f.write(value)


if __name__ == '__main__':
  main()
