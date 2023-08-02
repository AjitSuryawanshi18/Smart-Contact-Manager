
const toggleSidebar= () =>{

    if($(".sidebar").is(":visible")){

        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");


    }else{

        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");


    }
};

const search = () =>{
//console.log("Searching.....");
let query = $("#search-input").val();


if(query == ''){
    //not searched anything...
    $(".search-result").hide();
}else{
//search 
//console.log(query);

//sending request to the server

/*  it is provide whole url i.e. localhost:9091/user/contact/1 etc
var pathname = window.location.href;
console.log(pathname);*/

/*it is provide only pathname without localhost i.e. /user/contact/1  etc
var pathname1 = window.location.pathname;
console.log(pathname1);*/

/* it is provide only origin i.e. localhost:9091 etc */
var pathnamee = window.location.origin;
console.log(pathnamee)



let url=`${pathnamee}/search/${query}`;




fetch(url)
.then((response)=> {
return response.json(); })
.then((data) =>{
    //data we gets here...
    // console.log(data);

    let text=`<div class='list-group'>`

data.forEach((contact) => {
    text += `<a href='/user/${contact.cId}/contact' class="list-group-item list-group-action"> ${contact.name} </a>`
});

    text += `</div>`;

    $(".search-result").html(text);
    $(".search-result").show();
   
} );

 }
};

