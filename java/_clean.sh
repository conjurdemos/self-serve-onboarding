DIRLIST=". pas dap javarest"
for i in $DIRLIST; do
  pushd $i
  rm -f *.class *.jar manifest.txt *.jks temp.yml
  popd
done
