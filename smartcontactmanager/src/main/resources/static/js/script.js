
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
let url=`http://localhost:9091/search/${query}`;

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
