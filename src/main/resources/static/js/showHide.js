function showOrHide(elemId){
    let ele=document.getElementById(elemId);
    let sOrH=ele.style.display;
    if (sOrH === ''){
       ele.style.display='none';
    }else{
        ele.style.display='';
    }
}