function copyToClip(elemId) {

    let tx = document.getElementById(elemId)

    if (tx.style.display === 'none'){
        tx.style.display='';
    }

    // 创建select对象与range对象
    const selection = window.getSelection()
    const range = document.createRange()
    // 从当前selection对象中移除所有的range对象,
    // 取消所有的选择只 留下anchorNode 和focusNode属性并将其设置为null。
    // 这里没弄明白为什么需要先remove一下， 也没有太多资料查证 没有这句会复制失败
    if(selection.rangeCount > 0) selection.removeAllRanges()
    // 使 Range 包含某个节点的内容 使用这个 或者下面的selectNode都可以
    // range.selectNodeContents(tx)

    // 使 Range 包含某个节点及其内容
    range.selectNode(tx)
    // 向选区（Selection）中添加一个区域（Range）
    selection.addRange(range)
    // 已复制文字
    // console.log('selectedText', selection.toString())
    // 执行浏览器复制命令
    document.execCommand('copy')
}