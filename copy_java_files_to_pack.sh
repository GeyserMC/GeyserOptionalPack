wget https://launcher.mojang.com/v1/objects/37fd3c903861eeff3bc24b71eed48f828b5269c8/client.jar
unzip client.jar -d extracted/
IFS=' '
while read -r p || [ -n "$p" ]; do
  read -rafilesToCopy<<< "$p"
  echo "extracted/${filesToCopy[0]} ${filesToCopy[1]}"
  mkdir -p "${filesToCopy[1]}"
  cp "extracted/${filesToCopy[0]}" "${filesToCopy[1]}"
done <required_files.txt
rm client.jar
rm -r extracted