#!/bin/sh

# 遍历 /app/source 的文件和目录，递归创建软链接
link_tree() {
  src_dir=$1
  dest_dir=$2


  for item in "$src_dir"/*; do
    rel_path=$(realpath --relative-to="$src_dir" "$item") # 计算相对路径
    if [ -d "$item" ]; then
      mkdir -p "$dest_dir/$rel_path"
      link_tree "$item" "$dest_dir/$rel_path"
    elif [ -f "$item" ]; then
      ln -s "$item" "$dest_dir/$rel_path"
    fi
  done
}

# 调用函数，将 /app/source 的内容链接到 /app
link_tree /app/source /app

# 启动 Jekyll
bundle exec jekyll serve --host 0.0.0.0 --destination $TEMP_DIR/_site