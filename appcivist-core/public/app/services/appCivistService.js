﻿// This handles retrieving data and is used by controllers. 
// 3 options (server, factory, provider) with 
// each doing the same thing just structuring the functions/data differently.
appCivistApp.service('appCivistService', function () {
    this.getAssemblies = function () {
        return assemblies;
    };

    // For getting the data from a SERVER, add Ajax Calls to the functions below
    this.insertAssembly = function (title, description, city) {
        var topID = assemblies.length + 1;
        assemblies.push({
            id: topID,
            title: title,
            description: description,
            city: city
        });
    };

    this.deleteAssembly = function (id) {
        for (var i = assemblies.length - 1; i >= 0; i--) {
            if (assemblies[i].id === id) {
                assemblies.splice(i, 1);
                break;
            }
        }
    };

    this.getAssembly = function (id) {
        for (var i = 0; i < assemblies.length; i++) {
            if (assemblies[i].id === id) {
                return assemblies[i];
            }
        }
        
        
        
        return null;
    };
    
    var Users = $resource('/user/:userId', { userId: '@id' });
    var user = Users.get({ userId: 123 }, function () {
      user.abc = true;
      user.$save();
    });

    
    
    var assemblies = [
        {
            id: 1, name: 'Urban Infrastructure', 
            description: 'An assembly about Urban Infrastructure. Image Credit: Centro Social Autogestionado La Tabacalera de Lavapiés. Plano Planta Principal.', 
            city: 'San Francisco', 
            icon: '/assets/images/tabacalera-140.png',
            url: '/assembly/1'
        },
        {
            id: 2, name: 'Institutional Racism', 
            description: 'An assembly about Institutional Racism. Image Credit: Favianna Rodriguez. La Justicia No Tiene Fronteras, 2013.', 
            city: 'Oakland',
            icon: '/assets/images/justicia-140.png',
            url: '/assembly/2'
        },
        {
            id: 3, name: 'Public Health', 
            description: 'An assembly about Public Health', 
            city: 'Los Angeles',
            icon: '/assets/images/barefootdoctor-140.png',
            url: '/assembly/3'
        },
        {
            id: 4, name: 'Public Health', 
            description: 'An assembly about Public Health', 
            city: 'Los Angeles',
            icon: '/assets/images/barefootdoctor-140.png',
            url: '/assembly/4'
        },
        {
            id: 5, name: 'Participatory Budgeting SF', 
            description: 'An assembly about PB in SF', 
            city: 'San Francisco',
            icon: '/assets/images/barefootdoctor-140.png',
            url: '/assembly/5',
            campaigns: [
                { 
                	campaignId: 1, name: 'Proposals', 
                	url : 'http://localhost:3000/g/hkP4Bvtn/proposals-for-sf-participatory-budgeting'
                }
            ]
        }
    ];

});